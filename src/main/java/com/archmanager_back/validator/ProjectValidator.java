// src/main/java/com/archmanager_back/validator/ProjectValidator.java
package com.archmanager_back.validator;

import com.archmanager_back.config.constant.AppProperties;
import com.archmanager_back.exception.ProjectValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectValidator {

    private final AppProperties props;

    /** Validate name input for createAndStart(...). */
    public void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProjectValidationException("Project name must not be empty");
        }
        int maxLen = props.getProject().getMaxNameLength();
        if (name.length() > maxLen) {
            throw new ProjectValidationException(
                "Project name must be at most " + maxLen + " characters");
        }
    }

    /** Validate slug & username input for connectProject(...). */
    public void validateConnectParams(String slug, String username) {
        if (slug == null || slug.isBlank()) {
            throw new ProjectValidationException("Project slug must not be empty");
        }
        if (username == null || username.isBlank()) {
            throw new ProjectValidationException("Username must not be empty");
        }
    }
}
