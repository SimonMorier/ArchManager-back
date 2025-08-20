package com.archmanager_back.api.graph.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeDTO {
    @NotNull(message = "Node id is mandatory")
    private Long id;

    @NotBlank(message = "Node type is mandatory")
    private String nodeType;

    @NotBlank(message = "Node name is mandatory")
    private String name;

    private String description = "";

    private String rawProperties = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setDescription(String description) {
        this.description = (description == null) ? "" : description;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setRawProperties(String rawProperties) {
        this.rawProperties = (rawProperties == null) ? "" : rawProperties;
    }
}