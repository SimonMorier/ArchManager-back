package com.archmanager_back.service;

import com.archmanager_back.context.Neo4jDriverRegistry;
import com.archmanager_back.context.UserProjectRegistry;
import com.archmanager_back.model.entity.neo4j.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphQueryService {

    private final UserProjectRegistry userProj;
    private final Neo4jDriverRegistry driverReg;

    public GraphEntity getFullGraphEntity(String username) {
        Long projectId = userProj.currentProjectId(username);
        Neo4jClient c = Neo4jClient.create(driverReg.getDriver(projectId));

        List<NodeEntity> nodes = c.query(
                "MATCH (n) RETURN id(n) AS id, labels(n) AS labels, properties(n) AS properties")
                .fetch().all().stream().map(row -> {
                    NodeEntity e = new NodeEntity();
                    e.setId(((Number) row.get("id")).longValue());
                    e.setLabels((List<String>) row.get("labels"));
                    e.setProperties((Map<String, Object>) row.get("properties"));
                    return e;
                }).collect(Collectors.toList());

        List<LinkEntity> links = c.query(
                "MATCH (a)-[r]->(b) RETURN id(a) AS source, id(b) AS target, type(r) AS relation")
                .fetch().all().stream().map(row -> {
                    LinkEntity l = new LinkEntity();
                    l.setSource(((Number) row.get("source")).longValue());
                    l.setTarget(((Number) row.get("target")).longValue());
                    l.setRelation((String) row.get("relation"));
                    return l;
                }).collect(Collectors.toList());

        GraphEntity g = new GraphEntity();
        g.setNodes(nodes);
        g.setLinks(links);
        return g;
    }

    public List<Map<String, Object>> countNodes(String username) {
        Long projectId = userProj.currentProjectId(username);
        return (List<Map<String, Object>>) Neo4jClient.create(driverReg.getDriver(projectId))
                .query("MATCH (n) RETURN count(n) AS totalNodes")
                .fetch().all();
    }
}
