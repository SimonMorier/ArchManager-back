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

    public static NodeTypeEnum parentOf(NodeTypeEnum kind, ScaleStrategy strat) {
        return switch (strat) {
            case PHYSICAL -> PHYSICAL.get(kind);
            case LOGICAL -> LOGICAL.get(kind);
        };
    }

    public static int rank(NodeTypeEnum kind, ScaleStrategy strat) {
        return switch (strat) {
            case PHYSICAL -> switch (kind) {
                case Variable -> 0;
                case Operation -> 1;
                case Type -> 2;
                case File -> 3;
                case Folder -> 4;
                case Project -> 5;
                default -> 99;
            };
            case LOGICAL -> switch (kind) {
                case Variable -> 0;
                case Operation -> 1;
                case Type -> 2;
                case Scope -> 3;
                default -> 99;
            };
        };
    }
}
