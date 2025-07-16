package com.archmanager_back.model.dto;

import com.archmanager_back.model.domain.RoleEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PermissionDTO {
    private Long projectId;
    private String projectSlug;
    private RoleEnum role;
}