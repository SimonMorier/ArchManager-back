package com.archmanager_back.model.dto.graph;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeDTO {
    private Long id;
    private String nodeType;
    private String name;
    private String description;
    private String rawProperties;
}