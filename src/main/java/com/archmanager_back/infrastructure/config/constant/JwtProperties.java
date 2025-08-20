package com.archmanager_back.infrastructure.config.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long expirationMs;
    private String tokenPrefix = "Bearer ";
    private String header = "Authorization";
}
