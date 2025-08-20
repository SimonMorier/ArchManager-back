package com.archmanager_back.application.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.archmanager_back.api.graph.dto.GraphFilterDTO;
import com.archmanager_back.domain.graph.GraphEntity;
import com.archmanager_back.domain.graph.LinkEntity;
import com.archmanager_back.domain.graph.NodeEntity;
import com.archmanager_back.domain.graph.NodeTypeEnum;
import com.archmanager_back.domain.graph.ScaleStrategy;
import com.archmanager_back.infrastructure.persistence.neo4j.GraphQueryExecutor;
import com.archmanager_back.infrastructure.persistence.neo4j.query.CypherQuery;
import com.archmanager_back.infrastructure.persistence.neo4j.query.FullGraphQuery;
import com.archmanager_back.infrastructure.persistence.neo4j.query.NodeCountQuery;
import com.archmanager_back.infrastructure.persistence.neo4j.query.ScaledGraphQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphQueryService {

        private final GraphQueryExecutor executor;
        private final FullGraphQuery fullGraphQuery;
        private final NodeCountQuery nodeCountQuery;
        private final ScaledGraphQuery scaledQuery;

        public GraphEntity getFullGraphEntity(String username) {
                return executor.run(username, fullGraphQuery);
        }

        public List<Map<String, Object>> countNodes(String username) {
                return executor.run(username, nodeCountQuery);
        }

        public GraphEntity getScaledGraphEntity(String username,
                        NodeTypeEnum level,
                        int hops,
                        ScaleStrategy strategy,
                        Set<NodeTypeEnum> extraTypes,
                        GraphFilterDTO filter) {

                CypherQuery<GraphEntity> query = scaledQuery.withParams(level, hops, strategy, extraTypes);

                return applyFilter(executor.run(username, query), filter);
        }

        private GraphEntity applyFilter(GraphEntity g, GraphFilterDTO f) {

                if (f == null ||
                                ((f.getNodeTypes() == null || f.getNodeTypes().isEmpty()) &&
                                                (f.getLinkLabels() == null || f.getLinkLabels().isEmpty()))) {

                        return g;
                }

                Set<String> allowedNode = f.getNodeTypes() == null
                                ? Set.of()
                                : f.getNodeTypes().stream().map(Enum::name).collect(Collectors.toSet());
                Set<String> allowedLink = f.getLinkLabels() == null
                                ? Set.of()
                                : f.getLinkLabels().stream().map(Enum::name).collect(Collectors.toSet());

                List<NodeEntity> keptNodes = g.getNodes();
                if (!allowedNode.isEmpty()) {
                        keptNodes = keptNodes.stream()
                                        .filter(n -> n.getLabels().stream().anyMatch(allowedNode::contains))
                                        .toList();

                }

                Set<Long> keptIds = keptNodes.stream()
                                .map(NodeEntity::getId)
                                .collect(Collectors.toSet());

                List<LinkEntity> keptLinks = g.getLinks();
                if (!allowedLink.isEmpty()) {
                        keptLinks = keptLinks.stream()
                                        .filter(l -> allowedLink.contains(l.getRelation()))
                                        .toList();

                }
                keptLinks = keptLinks.stream()
                                .filter(l -> keptIds.contains(l.getSource()) && keptIds.contains(l.getTarget()))
                                .toList();

                Set<Long> linkedIds = keptLinks.stream()
                                .flatMap(l -> Stream.of(l.getSource(), l.getTarget()))
                                .collect(Collectors.toSet());
                List<NodeEntity> finalNodes = keptNodes.stream()
                                .filter(n -> linkedIds.isEmpty() || linkedIds.contains(n.getId()))
                                .toList();

                GraphEntity result = new GraphEntity(finalNodes, keptLinks);

                return result;
        }
}
