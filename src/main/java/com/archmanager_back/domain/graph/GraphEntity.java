package com.archmanager_back.domain.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GraphEntity {
    private List<NodeEntity> nodes;
    private List<LinkEntity> links;
}
