package com.archmanager_back.infrastructure.persistence.neo4j.query;

import org.springframework.data.neo4j.core.Neo4jClient;

public interface CypherQuery<R> {
    R execute(Neo4jClient client);
}