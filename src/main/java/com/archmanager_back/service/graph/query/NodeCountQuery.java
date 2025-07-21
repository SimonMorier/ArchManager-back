package com.archmanager_back.service.graph.query;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class NodeCountQuery implements CypherQuery<List<Map<String, Object>>> {

    @Override
    public List<Map<String, Object>> execute(Neo4jClient c) {
        return (List<Map<String, Object>>) c.query("MATCH (n) RETURN count(n) AS totalNodes").fetch().all();
    }
}
