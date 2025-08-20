package com.archmanager_back.domain.graph;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class NodeEntity {
    private Long id;
    private List<String> labels;
    private Map<String, Object> properties;
}
