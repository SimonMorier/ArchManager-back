package com.archmanager_back.service.graph;

import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.dto.graph.LinkDTO;
import com.archmanager_back.model.dto.graph.NodeDTO;
import com.archmanager_back.model.dto.graph.update.GraphPatchDTO;
import com.archmanager_back.model.dto.graph.update.NodeUpdateDTO;
import com.archmanager_back.model.dto.graph.update.RelationDeleteDTO;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.model.entity.neo4j.LinkEntity;
import com.archmanager_back.model.entity.neo4j.NodeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphMutationService {

    private final GraphQueryService queryService;
    private final GraphImportService importService;

    /**
     * Récupère le graphe, applique le patch et réimporte.
     * Retourne le graphe final (DTO) pour le client.
     */
    public GraphDTO mutateAndReimport(String username, GraphPatchDTO patch) {

        // 1) Récupérer le graphe actuel
        GraphEntity current = queryService.getFullGraphEntity(username);

        // 2) Convertir en structures modifiables (NodeDTO / LinkDTO)
        Map<Long, NodeDTO> nodesById = current.getNodes().stream()
                .map(this::toNodeDTO)
                .collect(Collectors.toMap(NodeDTO::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        // Set pour éviter les doublons de liens: clé = src|tgt|rel
        Map<String, LinkDTO> linksByKey = current.getLinks().stream()
                .map(this::toLinkDTO)
                .collect(Collectors.toMap(
                        l -> linkKey(l.getSource(), l.getTarget(), l.getRelation()),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new));

        // 3) Appliquer le patch

        // 3.1 Supprimer des nœuds + supprimer leurs liens associés
        Set<Long> toDeleteNodeIds = new HashSet<>(patch.getDeleteNodeIds() == null
                ? List.of()
                : patch.getDeleteNodeIds());
        if (!toDeleteNodeIds.isEmpty()) {
            toDeleteNodeIds.forEach(nodesById::remove);
            // Supprimer tous les liens touchant ces nœuds
            linksByKey.entrySet().removeIf(e -> {
                LinkDTO l = e.getValue();
                return toDeleteNodeIds.contains(l.getSource()) || toDeleteNodeIds.contains(l.getTarget());
            });
        }

        // 3.2 Mise à jour partielle des nœuds (champs non-nulls uniquement)
        if (patch.getUpdateNodes() != null) {
            for (NodeUpdateDTO upd : patch.getUpdateNodes()) {
                NodeDTO existing = nodesById.get(upd.getId());
                if (existing == null) {
                    log.warn("Update skipped: node {} not found", upd.getId());
                    continue;
                }
                NodeDTO merged = mergeNode(existing, upd);
                nodesById.put(merged.getId(), merged);
            }
        }

        // 3.3 Upsert de nœuds (création ou remplacement complet sur ces champs)
        if (patch.getUpsertNodes() != null) {
            for (NodeDTO nd : patch.getUpsertNodes()) {
                nodesById.put(nd.getId(), sanitizeNode(nd));
            }
        }

        // 3.4 Suppression de liens précis
        if (patch.getDeleteLinks() != null) {
            for (RelationDeleteDTO del : patch.getDeleteLinks()) {
                linksByKey.remove(linkKey(del.getSource(), del.getTarget(), del.getRelation()));
            }
        }

        // 3.5 Upsert de liens (si les deux nœuds existent)
        if (patch.getUpsertLinks() != null) {
            for (LinkDTO l : patch.getUpsertLinks()) {
                if (nodesById.containsKey(l.getSource()) && nodesById.containsKey(l.getTarget())) {
                    linksByKey.put(linkKey(l.getSource(), l.getTarget(), l.getRelation()), l);
                } else {
                    log.warn("Link upsert skipped: missing node(s) src={} tgt={}", l.getSource(), l.getTarget());
                }
            }
        }

        // 4) Intégrité: retirer les liens orphelins (au cas où)
        Set<Long> existingIds = nodesById.keySet();
        linksByKey.entrySet().removeIf(e -> {
            LinkDTO l = e.getValue();
            return !existingIds.contains(l.getSource()) || !existingIds.contains(l.getTarget());
        });

        // 5) Construire le nouveau GraphDTO
        List<NodeDTO> finalNodes = new ArrayList<>(nodesById.values());
        List<LinkDTO> finalLinks = new ArrayList<>(linksByKey.values());
        GraphDTO updated = new GraphDTO(finalNodes, finalLinks);

        // 6) Réimporter complètement
        importService.importGraph(username, updated);

        // 7) Retourner le graphe final (tel que réimporté)
        return updated;
    }

    private String linkKey(Long src, Long tgt, String rel) {
        return src + "|" + tgt + "|" + rel;
    }

    private NodeDTO sanitizeNode(NodeDTO n) {
        // Empêche les nulls côté strings (cohérent avec vos setters)
        String name = n.getName() == null ? "" : n.getName();
        String desc = n.getDescription() == null ? "" : n.getDescription();
        String raw = n.getRawProperties() == null ? "" : n.getRawProperties();
        return new NodeDTO(n.getId(), n.getNodeType(), name, desc, raw);
    }

    private NodeDTO mergeNode(NodeDTO base, NodeUpdateDTO upd) {
        String name = upd.getName() != null ? upd.getName() : base.getName();
        String desc = upd.getDescription() != null ? upd.getDescription() : base.getDescription();
        String raw = upd.getRawProperties() != null ? upd.getRawProperties() : base.getRawProperties();
        String type = upd.getNodeType() != null ? upd.getNodeType() : base.getNodeType();
        return new NodeDTO(base.getId(), type, name, desc, raw);
    }

    private NodeDTO toNodeDTO(NodeEntity n) {
        // Choisit un label principal (le premier si présent)
        String nodeType = (n.getLabels() != null && !n.getLabels().isEmpty())
                ? n.getLabels().get(0)
                : "Node";
        Map<String, Object> props = n.getProperties() == null ? Map.of() : n.getProperties();
        String name = Objects.toString(props.getOrDefault("name", ""), "");
        String desc = Objects.toString(props.getOrDefault("description", ""), "");
        String raw = Objects.toString(props.getOrDefault("rawProperties", ""), "");
        return new NodeDTO(n.getId(), nodeType, name, desc, raw);
    }

    private LinkDTO toLinkDTO(LinkEntity l) {
        return new LinkDTO(l.getSource(), l.getTarget(), l.getRelation());
    }
}
