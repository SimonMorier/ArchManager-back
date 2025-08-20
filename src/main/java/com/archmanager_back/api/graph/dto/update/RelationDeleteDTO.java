package com.archmanager_back.api.graph.dto.update;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationDeleteDTO {
    private Long source;
    private Long target;
    private String relation;
}