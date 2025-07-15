package com.archmanager_back.service.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.archmanager_back.model.entity.Project;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.service.DockerProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdleContainerReaper {

    private final ProjectRepository projectRepo;
    private final DockerProjectService dockerService;

    private static final Duration IDLE_THRESHOLD = Duration.ofMinutes(1);

    @Scheduled(fixedRate = 600_000)
    public void reap() {
        Instant cutoff = Instant.now().minus(IDLE_THRESHOLD);

        List<Project> toStop = projectRepo.findAll().stream()
                .filter(p -> p.getActiveSessionCount() == 0)
                .filter(p -> p.getLastActivity() != null && p.getLastActivity().isBefore(cutoff))
                .toList();

        log.debug("IdleContainerReaper running: found {} idle projects to stop", toStop.size());

        for (Project p : toStop) {
            log.info("Stopping idle container for project '{}', containerId={}", p.getSlug(), p.getContainerId());
            dockerService.stopContainer(p.getContainerId());
            p.setContainerId(null);
            projectRepo.save(p);
        }
    }
}
