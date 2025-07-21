package com.archmanager_back.model.entity.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkEntity {
    private Long source;
    private Long target;
    private String relation;
}
