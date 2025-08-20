package com.archmanager_back.shared.util;

import com.archmanager_back.domain.graph.NodeEntity;
import com.archmanager_back.domain.graph.NodeTypeEnum;

public final class NodeKindUtil {

    private NodeKindUtil() {
    }

    public static NodeTypeEnum kindOf(NodeEntity n) {
        if (n.getLabels() == null || n.getLabels().isEmpty()) {
            throw new IllegalStateException("Node without label: " + n.getId());
        }
        return NodeTypeEnum.valueOf(n.getLabels().get(0));
    }
}
