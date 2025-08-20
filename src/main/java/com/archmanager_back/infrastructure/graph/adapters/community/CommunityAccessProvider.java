package com.archmanager_back.infrastructure.graph.adapters.community;

import com.archmanager_back.infrastructure.graph.spi.GraphAccessProvider;
import com.archmanager_back.infrastructure.runtime.UserProjectRegistry;

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityAccessProvider implements GraphAccessProvider {

    private final UserProjectRegistry userProj;
    private final Neo4jDriverRegistry driverReg;

    @Override
    public Neo4jClient clientFor(String username) {
        Long projectId = userProj.currentProjectId(username);
        Driver driver = driverReg.getDriver(projectId);
        return Neo4jClient.create(driver);
    }

    @Override
    public Session sessionFor(String username) {
        Long projectId = userProj.currentProjectId(username);
        Driver driver = driverReg.getDriver(projectId);
        return driver.session();
    }

    @Override
    public void closeProject(Long projectId) {
        driverReg.close(projectId);
    }
}
