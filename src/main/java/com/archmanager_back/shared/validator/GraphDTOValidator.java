package com.archmanager_back.shared.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.archmanager_back.api.graph.dto.GraphDTO;
import com.archmanager_back.api.graph.dto.LinkDTO;
import com.archmanager_back.api.graph.dto.NodeDTO;
import com.archmanager_back.domain.graph.LinkLabelEnum;
import com.archmanager_back.domain.graph.NodeTypeEnum;

import java.util.EnumSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GraphDTOValidator implements Validator {

    private final Set<String> validNodeTypes = EnumSet.allOf(NodeTypeEnum.class).stream()
            .map(Enum::name)
            .collect(java.util.stream.Collectors.toSet());

    private final Set<String> validRelations = EnumSet.allOf(LinkLabelEnum.class).stream()
            .map(Enum::name)
            .collect(java.util.stream.Collectors.toSet());

    @Override
    public boolean supports(Class<?> clazz) {
        return GraphDTO.class.isAssignableFrom(clazz);
    }

    public void format(GraphDTO graph) {
        graph.getNodes().forEach(node -> {
            String name = node.getName();
            if (name != null && !name.isEmpty()) {
                node.setName(name.substring(0, 1).toUpperCase() + name.substring(1));
            }
        });
        graph.getLinks().forEach(link -> {
            String rel = link.getRelation();
            if (rel != null && !rel.isEmpty()) {
                link.setRelation(rel.toUpperCase());
            }
        });
    }

    @Override
    public void validate(Object target, Errors errors) {
        GraphDTO graph = (GraphDTO) target;
        log.debug("Validating GraphDTO with {} nodes and {} links", graph.getNodes().size(), graph.getLinks().size());

        for (int i = 0; i < graph.getNodes().size(); i++) {
            NodeDTO node = graph.getNodes().get(i);
            if (!validNodeTypes.contains(node.getNodeType())) {
                log.warn("Invalid node type '{}' at index {}", node.getNodeType(), i);
                errors.rejectValue(
                        "nodes[" + i + "].nodeType",
                        "Invalid.nodeType",
                        new Object[] { node.getNodeType() },
                        "Node type is not recognized");
            }
        }

        for (int j = 0; j < graph.getLinks().size(); j++) {
            LinkDTO link = graph.getLinks().get(j);
            if (!validRelations.contains(link.getRelation())) {
                log.warn("Invalid relation '{}' at index {}", link.getRelation(), j);
                errors.rejectValue(
                        "links[" + j + "].relation",
                        "Invalid.relation",
                        new Object[] { link.getRelation() },
                        "Relationship type is not recognized");
            }
        }

        if (errors.hasErrors()) {
            log.info("GraphDTO validation failed: {} error(s)", errors.getErrorCount());
        } else {
            log.debug("GraphDTO validation passed");
        }
    }
}
