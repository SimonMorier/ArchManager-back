package com.archmanager_back.application.graph.composer;

import com.archmanager_back.domain.graph.HierarchyUtil;
import com.archmanager_back.domain.graph.NodeEntity;
import com.archmanager_back.domain.graph.NodeTypeEnum;
import com.archmanager_back.domain.graph.ScaleStrategy;
import com.archmanager_back.shared.mapper.GraphRowMapper;
import com.archmanager_back.shared.util.NodeKindUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GraphComposer {

    private final GraphRowMapper rowMapper;

    public NodeEntity climb(Neo4jClient client,
            NodeEntity start,
            NodeTypeEnum target,
            int hops,
            ScaleStrategy strat,
            Set<NodeTypeEnum> analysisNodes) {

        if (analysisNodes.contains(NodeKindUtil.kindOf(start))) {
            return start;
        }

        NodeEntity current = start;
        int climbed = 0;
        int max = (hops < 0) ? Integer.MAX_VALUE : hops;

        while (climbed < max && NodeKindUtil.kindOf(current) != target) {
            int currRank = HierarchyUtil.rank(NodeKindUtil.kindOf(current), strat);
            if (currRank > HierarchyUtil.rank(target, strat)) {
                break;
            }

            NodeTypeEnum parentKind = HierarchyUtil.parentOf(NodeKindUtil.kindOf(current), strat);
            if (parentKind == null) {
                break;
            }

            String rel = relationshipFor(NodeKindUtil.kindOf(current), strat);
            if (rel == null) {
                break;
            }

            String cy = "MATCH (c)" + rel + "(p:`" + parentKind + "`)" +
                    " WHERE id(c) = $id " +
                    " RETURN id(p)   AS id, " +
                    "        labels(p) AS labels, " +
                    "        properties(p) AS props";

            Map<String, Object> row = client.query(cy)
                    .bind(current.getId()).to("id")
                    .fetch().one().orElse(null);
            if (row == null) {
                break;
            }

            current = rowMapper.toNode(row, "");
            climbed++;
        }
        return current;
    }

    private static String relationshipFor(NodeTypeEnum child, ScaleStrategy s) {
        return switch (child) {
            case Variable -> "<-[:USES]-";
            case Operation -> "<-[:ENCAPSULATES]-";
            case Type -> (s == ScaleStrategy.PHYSICAL)
                    ? "<-[:DECLARES]-"
                    : "<-[:ENCLOSES]-";
            case File -> "<-[:CONTAINS]-";
            case Folder -> "<-[:INCLUDES]-";
            default -> null;
        };
    }
}
