package com.archmanager_back.context;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * username -> projectId actuellement connecté.
 */
@Component
public class UserProjectRegistry {

    private final ConcurrentMap<String, Long> map = new ConcurrentHashMap<>();

    public void connect(String username, Long projectId) {
        map.put(username, projectId);
    }

    public void disconnect(String username) {
        map.remove(username);
    }

    /**
     * @throws IllegalStateException si l'utilisateur n'est connecté à aucun projet.
     */
    public Long currentProjectId(String username) {
        return Optional.ofNullable(map.get(username))
                .orElseThrow(() -> new IllegalStateException(
                        "User " + username + " is not connected to any project"));
    }
}
