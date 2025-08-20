package com.archmanager_back.shared.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.archmanager_back.api.user.dto.UserRequestDTO;
import com.archmanager_back.api.user.dto.UserResponseDTO;
import com.archmanager_back.domain.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDTO dto);

    UserResponseDTO toResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequestDTO dto, @MappingTarget User user);

}
