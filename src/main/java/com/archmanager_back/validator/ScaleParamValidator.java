package com.archmanager_back.validator;

import com.archmanager_back.model.domain.NodeTypeEnum;
import com.archmanager_back.model.domain.ScaleStrategy;
import com.archmanager_back.model.domain.HierarchyUtil;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ScaleParamValidator {
    private static final Set<String> ALLOWED_PARAMS = Set.of("level", "hops", "strategy");

    public void validateOrThrow(Map<String, String> allParams,
            NodeTypeEnum level,
            int hops,
            ScaleStrategy strategy) {
        for (String param : allParams.keySet()) {
            if (!ALLOWED_PARAMS.contains(param)) {
                throw new IllegalArgumentException(
                        "Unknown or misspelled parameter '" + param + "'. Allowed parameters: " + ALLOWED_PARAMS);
            }
        }

        if (strategy == null) {
            throw new IllegalArgumentException("Scale strategy is required");
        }

        if (level == null) {
            throw new IllegalArgumentException("Scale level is required");
        }
        if (HierarchyUtil.rank(level, strategy) == 99) {
            throw new IllegalArgumentException(
                    "Scale level '" + level + "' is not valid for strategy " + strategy);
        }

        if (hops < -1 || hops == 0) {
            throw new IllegalArgumentException(
                    "Scale hops must be -1 (unlimited) or a positive integer");
        }
    }
}
