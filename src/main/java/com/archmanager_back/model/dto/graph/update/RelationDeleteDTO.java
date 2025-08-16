package com.archmanager_back.model.dto.graph.update;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationDeleteDTO {
    private Long source;
    private Long target;
    private String relation;
}