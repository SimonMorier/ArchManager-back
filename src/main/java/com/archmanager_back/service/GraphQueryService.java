package com.archmanager_back.service;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import com.archmanager_back.util.LogUtils;
import com.archmanager_back.context.SessionNeo4jContext;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.model.entity.neo4j.LinkEntity;
import com.archmanager_back.model.entity.neo4j.NodeEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GraphQueryService {
    private final SessionNeo4jContext sessionCtx;

    public GraphEntity getFullGraphEntity() {
        Neo4jClient c = Neo4jClient.create(sessionCtx.getDriver());

        log.debug(LogUtils
                .userPrefixed(String.format("Requesting Neo4j at URI: %s", sessionCtx.getUri())));
        // – tous les nœuds
        List<NodeEntity> nodes = c.query(
                "MATCH (n) RETURN id(n) AS id, labels(n) AS labels, properties(n) AS properties")
                .fetch().all()
                .stream().map(row -> {
                    NodeEntity e = new NodeEntity();
                    e.setId(((Number) row.get("id")).longValue());
                    e.setLabels((List<String>) row.get("labels"));
                    e.setProperties((Map<String, Object>) row.get("properties"));
                    return e;
                }).collect(Collectors.toList());

        // – toutes les relations
        List<LinkEntity> links = c.query(
                "MATCH (a)-[r]->(b) RETURN id(a) AS source, id(b) AS target, type(r) AS relation")
                .fetch().all()
                .stream().map(row -> {
                    LinkEntity l = new LinkEntity();
                    l.setSource(((Number) row.get("source")).longValue());
                    l.setTarget(((Number) row.get("target")).longValue());
                    l.setRelation((String) row.get("relation"));
                    return l;
                }).collect(Collectors.toList());

        GraphEntity graph = new GraphEntity();
        graph.setNodes(nodes);
        graph.setLinks(links);
        return graph;
    }

    public List<Map<String, Object>> countNodes() {
        log.debug(LogUtils
                .userPrefixed(String.format("Requesting Neo4j at URI: %s", sessionCtx.getUri())));
        return (List<Map<String, Object>>) Neo4jClient.create(sessionCtx.getDriver())
                .query("MATCH (n) RETURN count(n) AS totalNodes")
                .fetch().all();
    }
}
