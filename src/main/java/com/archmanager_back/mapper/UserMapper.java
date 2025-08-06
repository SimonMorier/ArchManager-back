package com.archmanager_back.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.archmanager_back.model.dto.UserRequestDTO;
import com.archmanager_back.model.dto.UserResponseDTO;
import com.archmanager_back.model.entity.jpa.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDTO dto);

    UserResponseDTO toResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequestDTO dto, @MappingTarget User user);

}
