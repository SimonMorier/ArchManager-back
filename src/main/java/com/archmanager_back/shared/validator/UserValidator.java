package com.archmanager_back.shared.validator;

import static com.archmanager_back.infrastructure.config.constant.ErrorLabel.PASSWORD_EMPTY;
import static com.archmanager_back.infrastructure.config.constant.ErrorLabel.PASSWORD_TOO_SHORT;
import static com.archmanager_back.infrastructure.config.constant.ErrorLabel.REQUEST_BODY_NULL;
import static com.archmanager_back.infrastructure.config.constant.ErrorLabel.USERNAME_EMPTY;

import com.archmanager_back.api.user.dto.UserRequestDTO;
import com.archmanager_back.shared.exception.custom.UserValidationException;

public final class UserValidator {

    private UserValidator() {
    }

    public static void validateRegistration(UserRequestDTO dto) {
        if (dto == null) {
            throw new UserValidationException(REQUEST_BODY_NULL);
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new UserValidationException(USERNAME_EMPTY);
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new UserValidationException(PASSWORD_EMPTY);
        }
        if (dto.getPassword().length() < 8) {
            throw new UserValidationException(PASSWORD_TOO_SHORT);
        }
    }

    public static void validateLogin(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new UserValidationException(USERNAME_EMPTY);
        }
        if (password == null || password.isBlank()) {
            throw new UserValidationException(PASSWORD_EMPTY);
        }
    }
}
