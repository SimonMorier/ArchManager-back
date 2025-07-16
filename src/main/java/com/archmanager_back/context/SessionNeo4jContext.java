package com.archmanager_back.context;

import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.util.LogUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class SessionNeo4jContext {

    private final HttpSession session;
    private final ProjectRepository projectRepo;

    private Driver driver;
    private String uri;
    private Long loadedProjectId;

    private synchronized void ensureInitialized() {
        Long projectId = (Long) session.getAttribute("currentProjectId");
        if (projectId == null) {
            throw new IllegalStateException("No project in HTTP session – call /projects/connect first");
        }
        if (!projectId.equals(loadedProjectId)) {
            if (driver != null) {
                driver.close();
            }

            Project project = projectRepo.findById(projectId)
                    .orElseThrow(() -> new IllegalStateException("Unknown project id in session"));
            this.uri = "bolt://localhost:" + project.getBoltPort();

            log.debug(LogUtils.userPrefixed("Initializing driver for project {} at {}"), project.getSlug(), uri);

            this.driver = GraphDatabase.driver(
                    uri,
                    AuthTokens.basic("neo4j", project.getPassword()),
                    org.neo4j.driver.Config.builder()
                            .withMaxConnectionPoolSize(5)
                            .build());
            this.loadedProjectId = projectId;
        }
    }

    public Driver getDriver() {
        ensureInitialized();
        return driver;
    }

    public String getUri() {
        ensureInitialized();
        return uri;
    }
}
