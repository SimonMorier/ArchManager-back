package com.archmanager_back.model.dto;

import com.archmanager_back.model.domain.RoleEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermissionRequestDTO {
    @NotBlank(message = "Project slug is required")
    private String projectSlug;

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Role is required")
    private RoleEnum role;
}