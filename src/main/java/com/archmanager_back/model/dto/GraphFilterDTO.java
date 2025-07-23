package com.archmanager_back.model.dto;

import com.archmanager_back.model.domain.LinkLabelEnum;
import com.archmanager_back.model.domain.NodeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphFilterDTO {

    private List<NodeTypeEnum> nodeTypes;

    private List<LinkLabelEnum> linkLabels;
}
