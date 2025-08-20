package com.archmanager_back.api.graph.dto;

import java.util.List;

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