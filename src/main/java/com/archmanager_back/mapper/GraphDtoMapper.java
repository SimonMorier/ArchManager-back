package com.archmanager_back.mapper;

import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.dto.graph.NodeDTO;
import com.archmanager_back.model.dto.graph.LinkDTO;
import com.archmanager_back.model.entity.neo4j.GraphEntity;
import com.archmanager_back.model.entity.neo4j.NodeEntity;
import com.archmanager_back.model.entity.neo4j.LinkEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface GraphDtoMapper {

    @Mappings({
            @Mapping(target = "id", source = "entity.id"),
            @Mapping(target = "nodeType", expression = "java(entity.getLabels().isEmpty() ? null : entity.getLabels().get(0))"),
            @Mapping(target = "name", expression = "java(String.valueOf(entity.getProperties().getOrDefault(\"name\", \"\")))"),
            @Mapping(target = "description", expression = "java(String.valueOf(entity.getProperties().getOrDefault(\"description\", \"\")))"),
            @Mapping(target = "rawProperties", expression = "java(String.valueOf(entity.getProperties().getOrDefault(\"rawProperties\", \"\")))")
    })
    NodeDTO nodeEntityToDto(NodeEntity entity);

    @Mappings({
            @Mapping(target = "source", source = "entity.source"),
            @Mapping(target = "target", source = "entity.target"),
            @Mapping(target = "relation", source = "entity.relation")
    })
    LinkDTO linkEntityToDto(LinkEntity entity);

    GraphDTO graphEntityToDto(GraphEntity graph);
}
