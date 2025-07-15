package com.archmanager_back.service;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.archmanager_back.context.SessionNeo4jContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectQueryService {
    private final SessionNeo4jContext sessionCtx;

    public List<Map<String, Object>> countNodes() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.debug("[user : {}]Requesting Neo4j at URI: {}", username, sessionCtx.getUri());
        return (List<Map<String, Object>>) Neo4jClient.create(sessionCtx.getDriver())
                .query("MATCH (n) RETURN count(n) AS totalNodes")
                .fetch().all();
    }
}
