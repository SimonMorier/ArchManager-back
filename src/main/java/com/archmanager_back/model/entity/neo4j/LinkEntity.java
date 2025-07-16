package com.archmanager_back.model.entity.neo4j;

import lombok.Data;

@Data
public class LinkEntity {
    private Long source;
    private Long target;
    private String relation;
}
