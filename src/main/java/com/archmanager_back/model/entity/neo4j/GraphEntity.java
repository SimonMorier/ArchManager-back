package com.archmanager_back.model.entity.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GraphEntity {
    private List<NodeEntity> nodes;
    private List<LinkEntity> links;
}
