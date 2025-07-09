// src/main/java/com/archmanager_back/validator/UserValidator.java
package com.archmanager_back.validator;

import com.archmanager_back.exception.UserValidationException;
import com.archmanager_back.model.dto.UserRequestDTO;

public final class UserValidator {

    private UserValidator() {}

    /** Valide les données reçues lors de l’inscription. */
    public static void validateRegistration(UserRequestDTO dto) {
        if (dto == null) {
            throw new UserValidationException("Request body must not be null");
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new UserValidationException("Username must not be empty");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new UserValidationException("Password must not be empty");
        }
        if (dto.getPassword().length() < 8) {
            throw new UserValidationException("Password must be at least 8 characters");
        }
        // autres règles (regex, email, etc.) à ajouter ici
    }

    /** Valide les données reçues lors du login. */
    public static void validateLogin(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new UserValidationException("Username must not be empty");
        }
        if (password == null || password.isBlank()) {
            throw new UserValidationException("Password must not be empty");
        }
    }
}
