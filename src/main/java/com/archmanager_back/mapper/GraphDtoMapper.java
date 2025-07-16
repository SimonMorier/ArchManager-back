// src/main/java/com/archmanager_back/mapper/GraphDtoMapper.java
package com.archmanager_back.mapper;

import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.dto.graph.NodeDTO;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.model.entity.neo4j.LinkEntity;
import com.archmanager_back.model.entity.neo4j.NodeEntity;
import com.archmanager_back.model.dto.graph.LinkDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class GraphDtoMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mappings({
            @Mapping(target = "nodeType", expression = "java(entity.getLabels().isEmpty() ? null : entity.getLabels().get(0))"),
            @Mapping(target = "name", expression = "java(String.valueOf(entity.getProperties().getOrDefault(\"name\", \"\")))"),
            @Mapping(target = "description", expression = "java(String.valueOf(entity.getProperties().getOrDefault(\"description\", \"\")))"),
            @Mapping(target = "rawProperties", expression = "java(serialize(entity.getProperties()))")
    })
    public abstract NodeDTO nodeEntityToDto(NodeEntity entity);

    public abstract LinkDTO linkEntityToDto(LinkEntity entity);

    @Mapping(source = "nodes", target = "nodes")
    @Mapping(source = "links", target = "links")
    public abstract GraphDTO graphEntityToDto(GraphEntity graph);

    String serialize(Map<String, Object> props) {
        try {
            return objectMapper.writeValueAsString(props);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
