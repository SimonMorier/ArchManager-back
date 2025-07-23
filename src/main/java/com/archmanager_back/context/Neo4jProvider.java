package com.archmanager_back.context;

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Neo4jProvider {

    private final UserProjectRegistry userProj;
    private final Neo4jDriverRegistry driverReg;

    public Neo4jClient clientFor(String username) {
        Long projectId = userProj.currentProjectId(username);
        Driver driver = driverReg.getDriver(projectId);
        return Neo4jClient.create(driver);
    }

    public Session sessionFor(String username) {
        Long projectId = userProj.currentProjectId(username);
        Driver driver = driverReg.getDriver(projectId);
        return driver.session();
    }
}
