package com.archmanager_back.application.graph;

import com.archmanager_back.api.graph.dto.GraphDTO;
import com.archmanager_back.application.graph.importer.GraphDataImporter;
import com.archmanager_back.infrastructure.graph.spi.GraphAccessProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GraphImportService {

    private final GraphAccessProvider accessProvider;
    private final GraphDataImporter dataImporter;

    public void importGraph(String username, GraphDTO graph) {
        try (Session session = accessProvider.sessionFor(username)) {
            session.writeTransaction(tx -> {
                dataImporter.deleteGraph(tx);
                dataImporter.importGraph(tx, graph.getNodes(), graph.getLinks());
                return null;
            });
        }
    }

    public void deleteGraph(String username) {
        try (Session session = accessProvider.sessionFor(username)) {
            session.writeTransaction(tx -> {
                dataImporter.deleteGraph(tx);
                return null;
            });
        }
    }

}