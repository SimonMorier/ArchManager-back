package com.archmanager_back.infrastructure.graph.spi;

import lombok.Value;

@Value
public class ProjectConnection {
    String containerId;
    Integer boltPort;
    String databaseName;
}
