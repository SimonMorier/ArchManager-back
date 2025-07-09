package com.archmanager_back.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.archmanager_back.model.dto.PermissionDTO;
import com.archmanager_back.model.dto.UserRequestDTO;
import com.archmanager_back.model.dto.UserResponseDTO;
import com.archmanager_back.model.entity.Permission;
import com.archmanager_back.model.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDTO dto);

    UserResponseDTO toResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequestDTO dto, @MappingTarget User user);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectSlug", source = "project.slug")
    PermissionDTO toPermissionDTO(Permission perm);

}
