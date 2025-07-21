package com.archmanager_back.service.graph.query;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.archmanager_back.context.Neo4jProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraphQueryExecutor {

    private final Neo4jProvider clientProvider;

    public <R> R run(String username, CypherQuery<R> query) {
        Neo4jClient client = clientProvider.clientFor(username);
        return query.execute(client);
    }
}
