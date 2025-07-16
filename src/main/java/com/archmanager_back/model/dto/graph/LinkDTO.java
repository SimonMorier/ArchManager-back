package com.archmanager_back.model.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkDTO {
    private Long source; // maintenant un Long pour matcher id de NodeDTO
    private Long target;
    private String relation;
}