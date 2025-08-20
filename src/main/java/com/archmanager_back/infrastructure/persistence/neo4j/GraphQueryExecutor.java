package com.archmanager_back.infrastructure.persistence.neo4j;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.archmanager_back.infrastructure.graph.spi.GraphAccessProvider;
import com.archmanager_back.infrastructure.persistence.neo4j.query.CypherQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraphQueryExecutor {

    private final GraphAccessProvider accessProvider;

    public <R> R run(String username, CypherQuery<R> query) {
        Neo4jClient client = accessProvider.clientFor(username);
        return query.execute(client);
    }
}
