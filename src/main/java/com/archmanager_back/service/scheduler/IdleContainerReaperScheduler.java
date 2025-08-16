package com.archmanager_back.service.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.archmanager_back.config.constant.AppProperties;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.service.project.DockerProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdleContainerReaperScheduler {

    private final ProjectRepository projectRepo;
    private final DockerProjectService dockerService;
    private final AppProperties props;

    @Scheduled(fixedRateString = "#{@appProperties.project.idleRateMs}")
    public void reap() {
        Duration idleThreshold = Duration.ofMinutes(props.getProject().getIdleThreshold());
        Instant cutoff = Instant.now().minus(idleThreshold);

        List<Project> toStop = projectRepo
                .findByIsUpTrueAndLastActivityBefore(cutoff);

        log.debug("IdleContainerReaper running: found {} idle projects to stop", toStop.size());

        for (Project p : toStop) {
            String cid = p.getContainerId();
            if (cid != null) {
                log.info("Stopping idle container for project '{}', containerId={}", p.getSlug(), cid);
                dockerService.stopContainer(cid);
            } else {
                log.debug("Project '{}' has no container to stop.", p.getSlug());
            }
        }
    }
}
