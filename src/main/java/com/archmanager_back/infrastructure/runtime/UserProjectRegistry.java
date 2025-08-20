package com.archmanager_back.infrastructure.runtime;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class UserProjectRegistry {

    private final ConcurrentMap<String, Long> map = new ConcurrentHashMap<>();

    public void connect(String username, Long projectId) {
        map.put(username, projectId);
    }

    public void disconnect(String username) {
        map.remove(username);
    }

    public Long currentProjectId(String username) {
        return Optional.ofNullable(map.get(username))
                .orElseThrow(() -> new IllegalStateException(
                        "User " + username + " is not connected to any project"));
    }
}
