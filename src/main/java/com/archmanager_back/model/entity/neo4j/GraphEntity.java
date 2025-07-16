package com.archmanager_back.model.entity.neo4j;

import lombok.Data;
import java.util.List;

@Data
public class GraphEntity {
    private List<NodeEntity> nodes;
    private List<LinkEntity> links;
}
