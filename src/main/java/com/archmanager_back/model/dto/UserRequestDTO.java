package com.archmanager_back.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UserRequestDTO {
    private String username;
    private String firstname;
    private String lastname;
    private String password;
}