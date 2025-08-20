package com.archmanager_back.application.graph.importer;

import com.archmanager_back.api.graph.dto.LinkDTO;
import com.archmanager_back.api.graph.dto.NodeDTO;
import com.archmanager_back.infrastructure.config.constant.AppProperties;

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

        private final AppProperties props;

        /**
         * Imports the entire graph in chunks, grouping by node and relation types
         * to ensure batches contain only items of the same type.
         */
        public void importGraph(Transaction tx,
                        List<NodeDTO> nodes,
                        List<LinkDTO> links) {
                final int CHUNK_SIZE = props.getProject().getChunkSize();
                log.info("Starting graph import: {} nodes, {} relationships",
                                nodes.size(), links.size());

                // Group nodes by type and import each group in chunks
                Map<String, List<NodeDTO>> nodesByType = nodes.stream()
                                .collect(Collectors.groupingBy(NodeDTO::getNodeType));
                nodesByType.forEach((type, batchList) -> {
                        log.info("Importing {} nodes of type '{}' in up to {}-sized chunks",
                                        batchList.size(), type, CHUNK_SIZE);
                        chunkedImport(batchList, batch -> importNodesBatch(tx, type, batch));
                });

                // Group links by relation type and import each group in chunks
                Map<String, List<LinkDTO>> linksByRel = links.stream()
                                .collect(Collectors.groupingBy(LinkDTO::getRelation));
                linksByRel.forEach((relType, batchList) -> {
                        log.info("Importing {} relationships of type '{}' in up to {}-sized chunks",
                                        batchList.size(), relType, CHUNK_SIZE);
                        chunkedImport(batchList, batch -> importLinksBatch(tx, relType, batch));
                });

                log.info("Graph import completed.");
        }

        private void importNodesBatch(Transaction tx,
                        String nodeType,
                        List<NodeDTO> nodes) {
                String cypher = "" +
                                "UNWIND $batch AS row\n" +
                                "MERGE (n:`%s` {id: row.id})\n" +
                                "SET n += row.props";
                cypher = String.format(cypher, nodeType);

                List<Map<String, Object>> batch = nodes.stream()
                                .map(node -> Map.of(
                                                "id", node.getId(),
                                                "props", Map.of(
                                                                "name", node.getName(),
                                                                "description", node.getDescription(),
                                                                "rawProperties", node.getRawProperties())))
                                .collect(Collectors.toList());

                log.debug("Importing node batch [{}]: size={}, type={}",
                                nodeType, batch.size(), nodeType);
                tx.run(cypher, Values.parameters("batch", batch));
                log.info("Node batch imported: {} items of type '{}'", batch.size(), nodeType);
        }

        private void importLinksBatch(Transaction tx,
                        String relType,
                        List<LinkDTO> links) {
                String cypher = "" +
                                "UNWIND $batch AS rel\n" +
                                "MATCH (a {id: rel.src}), (b {id: rel.tgt})\n" +
                                "MERGE (a)-[r:`%s`]->(b)";
                cypher = String.format(cypher, relType);

                List<Map<String, Long>> batch = links.stream()
                                .map(link -> Map.of(
                                                "src", link.getSource(),
                                                "tgt", link.getTarget()))
                                .collect(Collectors.toList());

                log.debug("Importing link batch [{}]: size={}, relation={}",
                                relType, batch.size(), relType);
                tx.run(cypher, Values.parameters("batch", batch));
                log.info("Relationship batch imported: {} items of type '{}'", batch.size(), relType);
        }

        private <T> void chunkedImport(List<T> items, Consumer<List<T>> importer) {
                final int CHUNK_SIZE = props.getProject().getChunkSize();
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
