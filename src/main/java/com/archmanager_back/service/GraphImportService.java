// src/main/java/com/archmanager_back/service/GraphImportService.java
package com.archmanager_back.service;

import com.archmanager_back.context.Neo4jDriverRegistry;
import com.archmanager_back.context.UserProjectRegistry;
import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.dto.graph.LinkDTO;
import com.archmanager_back.model.dto.graph.NodeDTO;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphImportService {

    private final UserProjectRegistry userProj;
    private final Neo4jDriverRegistry driverReg;

    public void importGraph(String username, GraphDTO graph) {
        Long projectId = userProj.currentProjectId(username);

        try (Session s = driverReg.getDriver(projectId).session()) {
            s.writeTransaction(tx -> {
                for (NodeDTO node : graph.getNodes()) {
                    Map<String, Object> props = Map.of(
                            "name", node.getName(),
                            "description", node.getDescription(),
                            "rawProperties", node.getRawProperties());

                    String cy = String.format(
                            "MERGE (n:`%s` {id:$id}) SET n += $props", node.getNodeType());
                    tx.run(cy, Values.parameters("id", node.getId(), "props", props));
                }
                for (LinkDTO l : graph.getLinks()) {
                    String cy = String.format(
                            "MATCH (a {id:$src}), (b {id:$tgt}) MERGE (a)-[r:`%s`]->(b)",
                            l.getRelation());
                    tx.run(cy, Values.parameters("src", l.getSource(), "tgt", l.getTarget()));
                }
                return null;
            });
        }
    }
}
