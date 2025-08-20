// src/main/java/com/archmanager_back/service/DockerProjectService.java
package com.archmanager_back.infrastructure.docker;

import com.archmanager_back.domain.project.Project;
import com.archmanager_back.infrastructure.config.constant.AppProperties;
import com.archmanager_back.infrastructure.config.constant.DockerHealthConstants;
import com.archmanager_back.infrastructure.persistence.jpa.ProjectRepository;
import com.archmanager_back.shared.util.LogUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerProjectService {

    private final AppProperties props;
    private final DockerClient docker;
    private final ProjectRepository projectRepo;

    /**
     * Creates, starts, health‐checks a Neo4j container and returns its ID + host
     * port.
     */
    public ContainerInstance provisionContainer(
            String slug,
            String password,
            String volumeName) throws InterruptedException {
        // 1) volume & image
        docker.createVolumeCmd().withName(volumeName).exec();
        String image = props.getDocker().getNeo4jImage();
        docker.pullImageCmd(image).start().awaitCompletion();

        // 2) internal/external ports
        ExposedPort boltInternal = ExposedPort.tcp(props.getNeo4j().getBoltPort());
        Ports bindings = new Ports();
        // let Docker pick an ephemeral host port
        bindings.bind(boltInternal, Ports.Binding.empty());

        // 3) healthcheck
        HealthCheck hc = buildHealthCheck();

        // 4) create
        CreateContainerResponse ctr = docker.createContainerCmd(image)
                .withName(slug)
                .withEnv(props.getNeo4j().getAuthUrl() + password)
                .withExposedPorts(boltInternal)
                .withHealthcheck(hc)
                .withHostConfig(HostConfig.newHostConfig()
                        .withPortBindings(bindings)
                        .withBinds(new Bind(volumeName, new Volume(props.getDocker().getVolumePath())))
                        .withMemory(props.getDocker().getMemoryLimit()))
                .exec();

        // 5) start & await healthy
        startAndAwait(ctr.getId(), props.getHealthcheck().getTimeout());

        // 6) inspect the actual host port
        InspectContainerResponse info = docker.inspectContainerCmd(ctr.getId()).exec();
        String spec = info.getNetworkSettings()
                .getPorts()
                .getBindings()
                .get(boltInternal)[0]
                .getHostPortSpec();
        int hostBoltPort = Integer.parseInt(spec);

        return new ContainerInstance(ctr.getId(), hostBoltPort);
    }

    /**
     * Ensures the given project’s container is running. If a start fails due to
     * port conflicts or “304 already started”, it will recreate or ignore as
     * needed.
     */
    public void ensureProjectRunning(Project project) throws InterruptedException {
        String id = project.getContainerId();
        log.debug(LogUtils.userPrefixed("Container {} check to connect"), project.getSlug());
        InspectContainerResponse preInfo = docker.inspectContainerCmd(id).exec();
        if (preInfo.getState().getRunning()) {
            syncPortFromContainer(project, preInfo);
            return;
        }

        try {
            docker.startContainerCmd(id).exec();
            awaitHealthy(id, props.getHealthcheck().getTimeout());
            InspectContainerResponse postInfo = docker.inspectContainerCmd(id).exec();
            syncPortFromContainer(project, postInfo);

        } catch (RuntimeException ex) {
            // maybe another thread won the race?
            if (docker.inspectContainerCmd(id).exec().getState().getRunning()) {
                log.debug("Container {} already running (race win)", project.getSlug());
                awaitHealthy(id, props.getHealthcheck().getTimeout());
                return;
            }

            String msg = ex.getMessage();
            if (msg != null && msg.contains(DockerHealthConstants.PORTS_NOT_AVAILABLE_SUBSTRING)) {
                log.warn(String.format(DockerHealthConstants.PORT_COLLISION_LOG_TEMPLATE, project.getSlug()));
                recreateWithNewPort(project);

            } else if (msg != null && msg.contains(DockerHealthConstants.CONTAINER_ALREADY_STARTED_SUBSTRING)) {
                // 304 case
                awaitHealthy(id, props.getHealthcheck().getTimeout());

            } else {
                throw ex;
            }
        }
    }

    private void recreateWithNewPort(Project project) throws InterruptedException {
        String oldId = project.getContainerId();

        // stop & rm old
        try {
            docker.stopContainerCmd(oldId).exec();
        } catch (Exception e) {
            log.warn("Failed to stop old container {}: {}", oldId, e.getMessage());
        }

        // 2) remove the old container (ignore if it fails)
        try {
            docker.removeContainerCmd(oldId).exec();
        } catch (Exception e) {
            log.warn("Failed to remove old container {}: {}", oldId, e.getMessage());
        }

        // bind new ephemeral port
        ExposedPort bolt = ExposedPort.tcp(props.getNeo4j().getBoltPort());
        Ports bindings = new Ports();
        bindings.bind(bolt, Ports.Binding.empty());

        // new container
        CreateContainerResponse ctr = docker.createContainerCmd(props.getDocker().getNeo4jImage())
                .withName(project.getSlug())
                .withEnv(props.getNeo4j().getAuthUrl() + project.getPassword())
                .withExposedPorts(bolt)
                .withHealthcheck(buildHealthCheck())
                .withHostConfig(HostConfig.newHostConfig()
                        .withPortBindings(bindings)
                        .withBinds(new Bind(project.getVolumeName(), new Volume(props.getDocker().getVolumePath())))
                        .withMemory(props.getDocker().getMemoryLimit()))
                .exec();

        // start & healthy
        startAndAwait(ctr.getId(), props.getHealthcheck().getTimeout());

        // inspect new port
        InspectContainerResponse info = docker.inspectContainerCmd(ctr.getId()).exec();
        String spec = info.getNetworkSettings().getPorts().getBindings().get(bolt)[0].getHostPortSpec();
        project.setContainerId(ctr.getId());
        project.setBoltPort(Integer.parseInt(spec));
        log.debug("Recreated project {} with new container ID {} and host port {}",
                project.getSlug(), project.getContainerId(), project.getBoltPort());
        projectRepo.save(project);
    }

    private void startAndAwait(String containerId, Duration timeout) throws InterruptedException {
        docker.startContainerCmd(containerId).exec();
        awaitHealthy(containerId, timeout);
    }

    private void awaitHealthy(String containerId, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            var state = docker.inspectContainerCmd(containerId).exec().getState();
            String status = state.getHealth() != null
                    ? state.getHealth().getStatus()
                    : null;
            if (state.getRunning() &&
                    (status == null || DockerHealthConstants.HEALTH_STATUS_HEALTHY.equalsIgnoreCase(status))) {
                return;
            }
            if (DockerHealthConstants.HEALTH_STATUS_UNHEALTHY.equalsIgnoreCase(status)) {
                throw new IllegalStateException(
                        String.format(DockerHealthConstants.ERROR_UNHEALTHY_TEMPLATE, containerId));
            }
            sleep(DockerHealthConstants.HEALTH_CHECK_SLEEP_MS);
        }
        throw new IllegalStateException(
                String.format(DockerHealthConstants.ERROR_TIMEOUT_TEMPLATE, containerId));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private HealthCheck buildHealthCheck() {
        String cmd = String.format(
                props.getHealthcheck().getCmdTemplate(),
                props.getNeo4j().getHttpPort());
        return new HealthCheck()
                .withTest(List.of(
                        props.getHealthcheck().getCmdShell(),
                        cmd))
                .withInterval(props.getHealthcheck().getInterval().toNanos())
                .withTimeout(props.getHealthcheck().getTimeout().toNanos())
                .withStartPeriod(props.getHealthcheck().getStartPeriod().toNanos())
                .withRetries(props.getHealthcheck().getRetries());
    }

    private void syncPortFromContainer(Project project, InspectContainerResponse info) {
        ExposedPort bolt = ExposedPort.tcp(props.getNeo4j().getBoltPort());
        String hostPortSpec = info.getNetworkSettings()
                .getPorts()
                .getBindings()
                .get(bolt)[0]
                .getHostPortSpec();
        int actualPort = Integer.parseInt(hostPortSpec);

        if (project.getBoltPort() == null || project.getBoltPort() != actualPort) {
            project.setBoltPort(actualPort);
            projectRepo.save(project);
            log.debug("Project {} updated with port {}", project.getSlug(), actualPort);
        }
    }

    public void stopContainer(String containerId) {
        try {
            var info = docker.inspectContainerCmd(containerId).exec();
            if (!info.getState().getRunning()) {
                log.info("Container {} is already stopped.", containerId);
                return;
            }
            docker.stopContainerCmd(containerId).exec();
        } catch (com.github.dockerjava.api.exception.NotFoundException nf) {
            log.warn("Container {} not found, removing project from DB.", containerId);
            projectRepo.findByContainerId(containerId).ifPresent(projectRepo::delete);
        } catch (Exception e) {
            log.warn("Failed to stop container {}: {}", containerId, e.getMessage());
        }
    }
}
