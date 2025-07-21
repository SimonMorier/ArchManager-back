package com.archmanager_back.service.graph.query;

import com.archmanager_back.mapper.GraphRowMapper;
import com.archmanager_back.model.domain.NodeTypeEnum;
import com.archmanager_back.model.domain.ScaleStrategy;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.model.entity.neo4j.LinkEntity;
import com.archmanager_back.model.entity.neo4j.NodeEntity;
import com.archmanager_back.service.graph.composer.GraphComposer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ScaledGraphQuery {

    private final GraphRowMapper rowMapper;
    private final GraphComposer composer;

    /**
     * @param level      niveau de composition cible (Type, File, Scope, …)
     * @param hops       nombre de niveaux à remonter (>=1)
     * @param strategy   PHYSICAL ou LOGICAL
     * @param extraTypes nœuds à préserver tels quels (Dimension, Category, Metric…)
     * @return un CypherQuery prêt à être passé à l’exécuteur
     */
    public CypherQuery<GraphEntity> withParams(NodeTypeEnum level,
            int hops,
            ScaleStrategy strategy,
            Set<NodeTypeEnum> extra) {

        return client -> {

            List<Map<String, Object>> rows = (List<Map<String, Object>>) client.query("""
                        MATCH (n)-[r]->(m)
                        RETURN id(n) AS nid, labels(n) AS nlabels, properties(n) AS nprops,
                               type(r) AS rtype,
                               id(m) AS mid, labels(m) AS mlabels, properties(m) AS mprops
                    """).fetch().all();

            Map<Long, NodeEntity> nodeMap = new HashMap<>();
            List<LinkEntity> links = new ArrayList<>();
            Set<String> linkSeen = new HashSet<>();

            for (var row : rows) {
                NodeEntity n0 = rowMapper.toNode(row, "n");
                NodeEntity m0 = rowMapper.toNode(row, "m");

                NodeEntity n = composer.climb(client, n0, level, hops, strategy, extra);
                NodeEntity m = composer.climb(client, m0, level, hops, strategy, extra);

                // on ne garde que les nœuds montés avec succès
                if (n == null || m == null)
                    continue;

                String key = n.getId() + "->" + m.getId() + "|" + row.get("rtype");
                // pas d’auto-lien ; pas de doublon
                if (n.getId() != m.getId() && linkSeen.add(key)) {
                    links.add(new LinkEntity(n.getId(), m.getId(), (String) row.get("rtype")));
                }

                nodeMap.putIfAbsent(n.getId(), n);
                nodeMap.putIfAbsent(m.getId(), m);
            }
            return new GraphEntity(new ArrayList<>(nodeMap.values()), links);
        };
    }
}
