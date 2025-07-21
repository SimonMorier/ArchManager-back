package com.archmanager_back.util;

import com.archmanager_back.model.domain.NodeTypeEnum;
import com.archmanager_back.model.entity.neo4j.NodeEntity;

public final class NodeKindUtil {

    private NodeKindUtil() {
    }

    /** Déduit le NodeTypeEnum du premier label. */
    public static NodeTypeEnum kindOf(NodeEntity n) {
        if (n.getLabels() == null || n.getLabels().isEmpty()) {
            throw new IllegalStateException("Node without label: " + n.getId());
        }
        return NodeTypeEnum.valueOf(n.getLabels().get(0));
    }
}
