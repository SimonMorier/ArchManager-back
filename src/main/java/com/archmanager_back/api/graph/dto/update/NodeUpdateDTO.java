package com.archmanager_back.api.graph.dto.update;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeUpdateDTO {
    private Long id;
    private String nodeType;
    private String name;
    private String description;
    private String rawProperties;
}