package com.archmanager_back.infrastructure.docker;

public record ContainerInstance(String containerId, int hostBoltPort) {
}
