package com.archmanager_back.api.project.dto;

import com.archmanager_back.domain.user.RoleEnum;

import lombok.Value;

@Value
public class ProjectPermissionDTO {
    String projectSlug;
    String username;
    String firstname;
    String lastname;
    RoleEnum role;
}
