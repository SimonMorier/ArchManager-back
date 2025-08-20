package com.archmanager_back.shared.mapper;

import com.archmanager_back.domain.graph.LinkEntity;
import com.archmanager_back.domain.graph.NodeEntity;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GraphRowMapper {

    @SuppressWarnings("unchecked")
    public NodeEntity toNode(Map<String, Object> row) {
        NodeEntity node = new NodeEntity();
        node.setId(((Number) row.get("id")).longValue());
        node.setLabels((List<String>) row.get("labels"));
        node.setProperties((Map<String, Object>) row.get("properties"));
        return node;
    }

    public LinkEntity toLink(Map<String, Object> row) {
        LinkEntity link = new LinkEntity();
        link.setSource(((Number) row.get("source")).longValue());
        link.setTarget(((Number) row.get("target")).longValue());
        link.setRelation((String) row.get("relation"));
        return link;
    }

    @SuppressWarnings("unchecked")
    public NodeEntity toNode(Map<String, Object> row,
            String prefix) {

        NodeEntity n = new NodeEntity();
        n.setId(((Number) row.get(prefix + "id")).longValue());
        n.setLabels((List<String>) row.get(prefix + "labels"));
        n.setProperties((Map<String, Object>) row.get(prefix + "props"));
        return n;
    }
}
