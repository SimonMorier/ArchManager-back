package com.archmanager_back.model.domain;

import static com.archmanager_back.model.domain.NodeTypeEnum.*;

import java.util.Map;

public final class HierarchyUtil {

    private static final Map<NodeTypeEnum, NodeTypeEnum> PHYSICAL = Map.of(
            Variable, Operation,
            Operation, Type,
            Type, File,
            File, Folder,
            Folder, Project);

    private static final Map<NodeTypeEnum, NodeTypeEnum> LOGICAL = Map.of(
            Variable, Operation,
            Operation, Type,
            Type, Scope);

    private HierarchyUtil() {
    }

    /**
     * Returns the *direct* parent type for the requested strategy,
     * or {@code null} if we are already at the root.
     */
    public static NodeTypeEnum parentOf(NodeTypeEnum kind, ScaleStrategy strat) {
        return switch (strat) {
            case PHYSICAL -> PHYSICAL.get(kind);
            case LOGICAL -> LOGICAL.get(kind);
        };
    }
}
