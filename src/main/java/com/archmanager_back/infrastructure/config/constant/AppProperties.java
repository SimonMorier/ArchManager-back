package com.archmanager_back.infrastructure.config.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Docker docker = new Docker();
    private Neo4j neo4j = new Neo4j();
    private Project project = new Project();
    private Healthcheck healthcheck = new Healthcheck();

    @Data
    public static class Docker {
        private String enginePath;
        private String neo4jImage;
        private Long memoryLimit;
        private int passwordLength;
        private String volumePath;
        private String host;

    }

    @Data
    public static class Neo4j {
        private int boltPort;
        private int httpPort;
        private String authUrl;
        private String boltPrefix;
    }

    @Data
    public static class Project {
        private String slugPrefix;
        private String volumeSuffix;
        private int maxNameLength;
        private int idleThreshold;
        private int idleRateMs;
        private int chunkSize;
    }

    @Data
    public static class Healthcheck {
        private Duration interval;
        private Duration timeout;
        private Duration startPeriod;
        private int retries;
        private String cmdShell;
        private String cmdTemplate;
    }
}
