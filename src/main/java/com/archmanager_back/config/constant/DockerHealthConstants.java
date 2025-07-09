package com.archmanager_back.config.constant;

public final class DockerHealthConstants {
    private DockerHealthConstants() {}

    public static final String HEALTH_STATUS_HEALTHY   = "healthy";
    public static final String HEALTH_STATUS_UNHEALTHY = "unhealthy";

    public static final String ERROR_UNHEALTHY_TEMPLATE = 
        "Container %s est unhealthy";
    public static final String ERROR_TIMEOUT_TEMPLATE   = 
        "Container %s n'est pas healthy à temps";

    public static final long   HEALTH_CHECK_SLEEP_MS    = 500L;

     // --- docker / réseau ---
    /** Sous-chaîne renvoyée par Docker quand le port hôte est déjà occupé. */
    public static final String PORTS_NOT_AVAILABLE_SUBSTRING = "ports are not available";

    /** Message de log lorsque l’on doit recréer un conteneur à cause du port. */
    public static final String PORT_COLLISION_LOG_TEMPLATE =
            "Port collision pour le projet %s – recréation du conteneur";

    public static final String CONTAINER_ALREADY_STARTED_SUBSTRING = "Status 304";
}
