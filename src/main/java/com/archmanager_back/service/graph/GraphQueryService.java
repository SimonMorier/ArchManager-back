package com.archmanager_back.service.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.archmanager_back.model.domain.NodeTypeEnum;
import com.archmanager_back.model.domain.ScaleStrategy;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.service.graph.query.CypherQuery;
import com.archmanager_back.service.graph.query.FullGraphQuery;
import com.archmanager_back.service.graph.query.GraphQueryExecutor;
import com.archmanager_back.service.graph.query.NodeCountQuery;
import com.archmanager_back.service.graph.query.ScaledGraphQuery;

import lombok.RequiredArgsConstructor;

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
            Set<NodeTypeEnum> extraTypes) {

        CypherQuery<GraphEntity> query = scaledQuery.withParams(level, hops, strategy, extraTypes);

        return executor.run(username, query);
    }
}
