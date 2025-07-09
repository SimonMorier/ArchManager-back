package com.archmanager_back.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class AuthResponseDTO {
    private UserResponseDTO user;
    private String token;
}
