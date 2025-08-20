package com.archmanager_back.infrastructure.config.constant;

public final class DockerHealthConstants {
    private DockerHealthConstants() {
    }

    public static final String HEALTH_STATUS_HEALTHY = "healthy";
    public static final String HEALTH_STATUS_UNHEALTHY = "unhealthy";

    public static final String ERROR_UNHEALTHY_TEMPLATE = "Container %s is unhealthy";
    public static final String ERROR_TIMEOUT_TEMPLATE = "Container %s did not become healthy in time";

    public static final long HEALTH_CHECK_SLEEP_MS = 500L;

    public static final String PORTS_NOT_AVAILABLE_SUBSTRING = "ports are not available";

    public static final String PORT_COLLISION_LOG_TEMPLATE = "Port collision for project %s – recreating container";

    public static final String CONTAINER_ALREADY_STARTED_SUBSTRING = "Status 304";
}
