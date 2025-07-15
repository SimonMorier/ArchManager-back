package com.archmanager_back.validator;

import com.archmanager_back.config.constant.AppProperties;
import com.archmanager_back.exception.ProjectValidationException;
import lombok.RequiredArgsConstructor;

import static com.archmanager_back.config.constant.ErrorLabel.PROJECT_NAME_EMPTY;
import static com.archmanager_back.config.constant.ErrorLabel.PROJECT_NAME_TOO_LONG;
import static com.archmanager_back.config.constant.ErrorLabel.PROJECT_SLUG_EMPTY;
import static com.archmanager_back.config.constant.ErrorLabel.USERNAME_EMPTY;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectValidator {

    private final AppProperties props;

    public void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProjectValidationException(PROJECT_NAME_EMPTY);
        }
        int maxLen = props.getProject().getMaxNameLength();
        if (name.length() > maxLen) {
            throw new ProjectValidationException(
                    String.format(PROJECT_NAME_TOO_LONG, maxLen));
        }
    }

    public void validateConnectParams(String slug, String username) {
        if (slug == null || slug.isBlank()) {
            throw new ProjectValidationException(PROJECT_SLUG_EMPTY);
        }
        if (username == null || username.isBlank()) {
            throw new ProjectValidationException(USERNAME_EMPTY);
        }
    }
}
