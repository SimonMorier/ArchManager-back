package com.archmanager_back.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.archmanager_back.config.constant.JwtProperties;

import javax.crypto.SecretKey;                  
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(
          props.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /** Generate a JWT (no deprecated API). */
    public String generateToken(String username) {
        return Jwts.builder()
                   .subject(username)         
                   .issuedAt(new Date())
                   .expiration(new Date(System.currentTimeMillis() + props.getExpirationMs()))
                   .signWith(getKey())                
                   .compact();
    }

    /** Extract username (sub). */
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()            
                            .verifyWith(getKey())     
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return claims.getSubject();
    }

    /** Validate signature & expiration. */
    public boolean validate(String token) {
        try {
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
