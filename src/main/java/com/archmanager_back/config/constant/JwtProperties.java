package com.archmanager_back.config.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class JwtProperties {
    /**
     * Clé secrète HMAC (≥ 64 caractères pour HS384).
     */
    private String secret;

    /**
     * Durée de validité du token en millisecondes.
     */
    private long expirationMs;

    private String tokenPrefix = "Bearer ";
    private String header = "Authorization";
}
