package com.archmanager_back.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import static com.archmanager_back.infrastructure.config.constant.ErrorLabel.INVALID_AUTH_TOKEN;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String body = "{\"error\":\"" + INVALID_AUTH_TOKEN + "\"}";
        try (PrintWriter writer = response.getWriter()) {
            writer.write(body);
        }
    }
}
