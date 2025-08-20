package com.archmanager_back.api.project.dto;

import lombok.Value;

@Value
public class ProjectDTO {
    String slug;
    String name;
    String boltUri;
    boolean isUp;
    String description;

    public static ProjectDTO from(com.archmanager_back.domain.project.Project p, String boltUri) {
        return new ProjectDTO(
                p.getSlug(),
                p.getName(),
                boltUri,
                p.isUp(),
                p.getDescription());
    }
}