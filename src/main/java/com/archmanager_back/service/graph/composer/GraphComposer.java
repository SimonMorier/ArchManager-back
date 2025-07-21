package com.archmanager_back.service.graph.composer;

import com.archmanager_back.mapper.GraphRowMapper;
import com.archmanager_back.model.domain.HierarchyUtil;
import com.archmanager_back.model.domain.NodeTypeEnum;
import com.archmanager_back.model.domain.ScaleStrategy;
import com.archmanager_back.model.entity.neo4j.NodeEntity;
import com.archmanager_back.util.NodeKindUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GraphComposer {

    private final GraphRowMapper rowMapper;

    /** Monte d’au plus {@code hops} niveaux jusqu’à {@code target}. */
    public NodeEntity climb(Neo4jClient client,
            NodeEntity start,
            NodeTypeEnum target,
            int hops,
            ScaleStrategy strat,
            Set<NodeTypeEnum> analysisNodes) {

        if (analysisNodes.contains(NodeKindUtil.kindOf(start)))
            return start;

        NodeEntity current = start;
        int climbed = 0;
        int max = (hops < 0) ? Integer.MAX_VALUE : hops;

        while (climbed < max && NodeKindUtil.kindOf(current) != target) {

            NodeTypeEnum parentKind = HierarchyUtil.parentOf(NodeKindUtil.kindOf(current), strat);
            if (parentKind == null)
                return null;

            String rel = relationshipFor(NodeKindUtil.kindOf(current), strat);
            if (rel == null)
                return null;
            String cy = "MATCH (c)" + rel + "(p:`" + parentKind + "`)" +
                    " WHERE id(c) = $id " +
                    " RETURN id(p)   AS id," +
                    "        labels(p) AS labels," +
                    "        properties(p) AS props";

            Map<String, Object> row = client.query(cy)
                    .bind(current.getId()).to("id")
                    .fetch().one().orElse(null);
            if (row == null)
                return null;

            current = rowMapper.toNode(row, ""); // préfixe vide : id / labels / props
            climbed++;
        }
        return (NodeKindUtil.kindOf(current) == target) ? current : null;
    }

    private static String relationshipFor(NodeTypeEnum child, ScaleStrategy s) {
        return switch (child) {
            /* Variable ──PARAMETERIZES/USES──> Operation */
            case Variable -> "-[:PARAMETERIZES|USES]->";

            /* Operation ◀─ENCAPSULATES── Type */
            case Operation -> "<-[:ENCAPSULATES]-";

            /*
             * Type ◀─DECLARES── File (physique)
             * ◀─ENCLOSES── Scope (logique)
             */
            case Type -> (s == ScaleStrategy.PHYSICAL)
                    ? "<-[:DECLARES]-"
                    : "<-[:ENCLOSES]-";

            /* File ◀─CONTAINS── Folder */
            case File -> "<-[:CONTAINS]-";

            /* Folder ◀─INCLUDES── Project */
            case Folder -> "<-[:INCLUDES]-";

            default -> null;
        };
    }

}
