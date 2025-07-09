package com.archmanager_back.model.dto;

import com.archmanager_back.model.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor @Data
public class PermissionDTO {
    private Long projectId;
    private String projectSlug;
    private Role role;
}