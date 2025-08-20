package com.archmanager_back.infrastructure.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.archmanager_back.domain.project.Project;
import com.archmanager_back.infrastructure.config.constant.AppProperties;
import com.archmanager_back.infrastructure.docker.DockerProjectService;
import com.archmanager_back.infrastructure.graph.adapters.community.Neo4jDriverRegistry;
import com.archmanager_back.infrastructure.persistence.jpa.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdleContainerReaperScheduler {

    private final ProjectRepository projectRepo;
    private final DockerProjectService dockerService;
    private final AppProperties props;
    private final Neo4jDriverRegistry driverRegistry;

    @Scheduled(fixedRateString = "#{@appProperties.project.idleRateMs}")
    public void reap() {
        Duration idleThreshold = Duration.ofMinutes(props.getProject().getIdleThreshold());
        Instant cutoff = Instant.now().minus(idleThreshold);

        List<Project> toStop = projectRepo
                .findByActiveSessionCountEqualsAndLastActivityBeforeAndContainerIdIsNotNull(0, cutoff);

        log.debug("IdleContainerReaper running: found {} idle projects to stop", toStop.size());

        for (Project p : toStop) {
            if (p.getActiveSessionCount() > 0) {
                log.info("Skip stopping project {}: sessions now {}", p.getSlug(), p.getActiveSessionCount());
                continue;
            }

            String cid = p.getContainerId();
            if (cid == null) {
                log.debug("Project '{}' has no container to stop.", p.getSlug());
                continue;
            }

            log.info("Stopping idle container for project '{}', containerId={}", p.getSlug(), cid);
            dockerService.stopContainer(cid);

            driverRegistry.close(p.getId());

            p.setUp(false);

            projectRepo.save(p);
        }
    }
}
