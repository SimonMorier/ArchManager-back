package com.archmanager_back.api.graph.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkDTO {
    @NotNull(message = "Link source is mandatory")
    private Long source;

    @NotNull(message = "Link target is mandatory")
    private Long target;

    @NotBlank(message = "Relation label is mandatory")
    private String relation;
}