package com.archmanager_back.model.dto;

import com.archmanager_back.model.domain.RoleEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequestDTO {
    @NotBlank(message = "Project slug is required")
    private String projectSlug;

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Role is required")
    private RoleEnum role;
}