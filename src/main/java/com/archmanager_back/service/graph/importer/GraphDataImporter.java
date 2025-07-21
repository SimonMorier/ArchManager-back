package com.archmanager_back.service.graph.importer;

import com.archmanager_back.model.dto.graph.LinkDTO;
import com.archmanager_back.model.dto.graph.NodeDTO;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GraphDataImporter {

    public void importNodes(Transaction tx, Iterable<NodeDTO> nodes) {
        for (NodeDTO node : nodes) {
            Map<String, Object> props = Map.of(
                    "name", node.getName(),
                    "description", node.getDescription(),
                    "rawProperties", node.getRawProperties());
            String cypher = String.format(
                    "MERGE (n:`%s` {id:$id}) SET n += $props",
                    node.getNodeType());
            tx.run(cypher, Values.parameters("id", node.getId(), "props", props));
        }
    }

    public void importLinks(Transaction tx, Iterable<LinkDTO> links) {
        for (LinkDTO link : links) {
            String cypher = String.format(
                    "MATCH (a {id:$src}), (b {id:$tgt}) MERGE (a)-[r:`%s`]->(b)",
                    link.getRelation());
            tx.run(cypher, Values.parameters("src", link.getSource(), "tgt", link.getTarget()));
        }
    }
}
