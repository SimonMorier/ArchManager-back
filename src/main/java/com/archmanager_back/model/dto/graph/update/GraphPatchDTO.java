package com.archmanager_back.model.dto.graph.update;

import com.archmanager_back.model.dto.graph.LinkDTO;
import com.archmanager_back.model.dto.graph.NodeDTO;
import lombok.*;
import java.util.*;

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