package com.archmanager_back.infrastructure.config;

import com.archmanager_back.domain.project.Project;
import com.archmanager_back.infrastructure.config.constant.AppProperties;
import com.archmanager_back.infrastructure.persistence.jpa.ProjectRepository;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
@Slf4j
@AllArgsConstructor
public class DynamicNeo4jDriverConfig {

    private final AppProperties props;

    @Bean
    @SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Driver neo4jDriver(AppProperties props,
            HttpSession session,
            ProjectRepository projectRepo) {

        Long projectId = (Long) session.getAttribute("currentProjectId");
        if (projectId == null) {
            throw new IllegalStateException("No project in HTTP session – call /projects/connect first");
        }
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalStateException("Unknown project id in session"));

        String uri = props.getNeo4j().getBoltPrefix() + props.getDocker().getHost() + project.getBoltPort();
        log.debug("Creating Neo4j driver for project {} at {}", project.getSlug(), uri);

        return GraphDatabase.driver(
                uri,
                AuthTokens.basic("neo4j", project.getPassword()),
                org.neo4j.driver.Config.builder()
                        .withMaxConnectionPoolSize(20)
                        .build());
    }
}
