package com.archmanager_back.context;

import com.archmanager_back.config.constant.AppProperties;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.repository.jpa.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * projectId -> Driver. Un seul driver par projet, partagé.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Neo4jDriverRegistry {

    private final AppProperties props;
    private final ProjectRepository projectRepo;
    private final ConcurrentMap<Long, Driver> drivers = new ConcurrentHashMap<>();

    public Driver getDriver(Long projectId) {
        return drivers.computeIfAbsent(projectId, this::createDriver);
    }

    private Driver createDriver(Long projectId) {
        Project p = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project " + projectId));

        String uri = props.getNeo4j().getBoltPrefix() + props.getDocker().getHost() + p.getBoltPort();
        log.debug("Create Neo4j driver for project {} -> {}", p.getSlug(), uri);

        return GraphDatabase.driver(uri,
                AuthTokens.basic("neo4j", p.getPassword()),
                Config.builder().withMaxConnectionPoolSize(10).build());
    }

    /** Optionnel : fermer toutes les connexions lors d’un shutdown graceful. */
    public void closeAll() {
        drivers.values().forEach(Driver::close);
        drivers.clear();
    }
}
