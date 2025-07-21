package com.archmanager_back.service.graph;

import com.archmanager_back.context.Neo4jProvider;
import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.service.graph.importer.GraphDataImporter;

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraphImportService {

    private final Neo4jProvider neo4jProvider;
    private final GraphDataImporter dataImporter;

    public void importGraph(String username, GraphDTO graph) {
        try (Session session = neo4jProvider.sessionFor(username)) {
            session.writeTransaction(tx -> {
                dataImporter.importNodes(tx, graph.getNodes());
                dataImporter.importLinks(tx, graph.getLinks());
                return null;
            });
        }
    }
}