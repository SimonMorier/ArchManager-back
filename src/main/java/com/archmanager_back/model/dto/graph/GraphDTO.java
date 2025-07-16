package com.archmanager_back.model.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphDTO {
    private List<NodeDTO> nodes;
    private List<LinkDTO> links;
}