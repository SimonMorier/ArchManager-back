package com.archmanager_back.infrastructure.graph.spi;

import org.neo4j.driver.Session;
import org.springframework.data.neo4j.core.Neo4jClient;

public interface GraphAccessProvider {
    Neo4jClient clientFor(String username);

    Session sessionFor(String username);

    void closeProject(Long projectId);
}
