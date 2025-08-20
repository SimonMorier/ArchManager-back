package com.archmanager_back.infrastructure.persistence.neo4j.query;

import com.archmanager_back.domain.graph.GraphEntity;
import com.archmanager_back.domain.graph.LinkEntity;
import com.archmanager_back.domain.graph.NodeEntity;
import com.archmanager_back.shared.mapper.GraphRowMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FullGraphQuery implements CypherQuery<GraphEntity> {

    private final GraphRowMapper mapper;

    @Override
    public GraphEntity execute(Neo4jClient client) {
        List<NodeEntity> nodes = client
                .query("MATCH (n) RETURN id(n) AS id, labels(n) AS labels, properties(n) AS properties")
                .fetch().all().stream()
                .map(mapper::toNode)
                .toList();

        List<LinkEntity> links = client
                .query("MATCH (a)-[r]->(b) RETURN id(a) AS source, id(b) AS target, type(r) AS relation")
                .fetch().all().stream()
                .map(mapper::toLink)
                .toList();

        return new GraphEntity(nodes, links);
    }
}
