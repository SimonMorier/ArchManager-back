package com.archmanager_back.shared.mapper;

import com.archmanager_back.api.graph.dto.GraphDTO;
import com.archmanager_back.api.graph.dto.LinkDTO;
import com.archmanager_back.api.graph.dto.NodeDTO;
import com.archmanager_back.domain.graph.GraphEntity;
import com.archmanager_back.domain.graph.LinkEntity;
import com.archmanager_back.domain.graph.NodeEntity;

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
