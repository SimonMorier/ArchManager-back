package com.archmanager_back.api.permission.dto;

import com.archmanager_back.domain.user.RoleEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PermissionDTO {
    private Long projectId;
    private String projectSlug;
    private RoleEnum role;
}