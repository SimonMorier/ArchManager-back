package com.archmanager_back.model.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphDTO {
    @NotEmpty(message = "Graph must contain at least one node")
    @Valid
    private List<NodeDTO> nodes;

    @NotEmpty(message = "Graph must contain at least one link")
    @Valid
    private List<LinkDTO> links;
}