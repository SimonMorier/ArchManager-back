package com.archmanager_back.api.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.archmanager_back.domain.graph.LinkLabelEnum;
import com.archmanager_back.domain.graph.NodeTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphFilterDTO {

    private List<NodeTypeEnum> nodeTypes;

    private List<LinkLabelEnum> linkLabels;
}
