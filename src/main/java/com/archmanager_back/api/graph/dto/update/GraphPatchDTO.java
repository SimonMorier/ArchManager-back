package com.archmanager_back.api.graph.dto.update;

import lombok.*;
import java.util.*;

import com.archmanager_back.api.graph.dto.LinkDTO;
import com.archmanager_back.api.graph.dto.NodeDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphPatchDTO {
    private List<NodeDTO> upsertNodes = Collections.emptyList();
    private List<NodeUpdateDTO> updateNodes = Collections.emptyList();
    private List<Long> deleteNodeIds = Collections.emptyList();

    private List<LinkDTO> upsertLinks = Collections.emptyList();
    private List<RelationDeleteDTO> deleteLinks = Collections.emptyList();
}