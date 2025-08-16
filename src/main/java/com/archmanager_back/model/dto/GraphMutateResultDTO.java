package com.archmanager_back.model.dto;

import java.util.List;

import com.archmanager_back.model.dto.graph.NodeDTO;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GraphMutateResultDTO {
    boolean success;
    String message;
    String projectSlug;
    List<NodeDTO> affectedNodes;
}