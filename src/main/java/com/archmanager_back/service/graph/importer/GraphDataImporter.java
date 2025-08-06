package com.archmanager_back.service.graph.importer;

import com.archmanager_back.model.dto.graph.LinkDTO;
import com.archmanager_back.model.dto.graph.NodeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GraphDataImporter {

        private static final int CHUNK_SIZE = 5000;

        /**
         * Imports the entire graph in chunks.
         */
        public void importGraph(Transaction tx,
                        List<NodeDTO> nodes,
                        List<LinkDTO> links) {
                log.info("Starting graph import: {} nodes, {} relationships",
                                nodes.size(), links.size());

                chunkedImport(nodes, batch -> importNodesBatch(tx, batch));
                chunkedImport(links, batch -> importLinksBatch(tx, batch));

                log.info("Graph import completed.");
        }

        private void importNodesBatch(Transaction tx, List<NodeDTO> nodes) {
                String cypher = ""
                                + "UNWIND $batch AS row\n"
                                + "MERGE (n:`%s` {id: row.id})\n"
                                + "SET n += row.props";
                cypher = String.format(cypher, nodes.get(0).getNodeType());

                List<Map<String, Object>> batch = nodes.stream()
                                .map(node -> Map.of(
                                                "id", node.getId(),
                                                "props", Map.of(
                                                                "name", node.getName(),
                                                                "description", node.getDescription(),
                                                                "rawProperties", node.getRawProperties())))
                                .collect(Collectors.toList());

                log.debug("Importing batch of {} nodes of type '{}'",
                                batch.size(), nodes.get(0).getNodeType());
                tx.run(cypher, Values.parameters("batch", batch));
                log.info("Node batch imported: {} items", batch.size());
        }

        private void importLinksBatch(Transaction tx, List<LinkDTO> links) {
                String cypher = ""
                                + "UNWIND $batch AS rel\n"
                                + "MATCH (a {id: rel.src}), (b {id: rel.tgt})\n"
                                + "MERGE (a)-[r:`%s`]->(b)";
                cypher = String.format(cypher, links.get(0).getRelation());

                List<Map<String, Long>> batch = links.stream()
                                .map(link -> Map.of(
                                                "src", link.getSource(),
                                                "tgt", link.getTarget()))
                                .collect(Collectors.toList());

                log.debug("Importing batch of {} relationships of type '{}'",
                                batch.size(), links.get(0).getRelation());
                tx.run(cypher, Values.parameters("batch", batch));
                log.info("Relationship batch imported: {} items", batch.size());
        }

        private <T> void chunkedImport(List<T> items, Consumer<List<T>> importer) {
                int total = items.size();
                int chunkCount = (total + CHUNK_SIZE - 1) / CHUNK_SIZE;
                for (int i = 0, part = 1; i < total; i += CHUNK_SIZE, part++) {
                        int end = Math.min(total, i + CHUNK_SIZE);
                        log.debug("Processing chunk {}/{} (items {} to {})",
                                        part, chunkCount, i + 1, end);
                        importer.accept(items.subList(i, end));
                }
        }

        /**
         * Deletes the entire graph.
         */
        public void deleteGraph(Transaction tx) {
                log.warn("Deleting entire graph...");
                tx.run("MATCH (n) DETACH DELETE n");
                log.info("Graph deleted.");
        }
}
