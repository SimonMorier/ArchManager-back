package com.archmanager_back.controller;

import com.archmanager_back.model.dto.*;
import com.archmanager_back.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UserRequestDTO request) {
        AuthResponseDTO response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserRequestDTO loginRequest) {
        AuthResponseDTO response = userService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/project/{projectName}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByProject(
            @PathVariable String projectName) {
        List<UserResponseDTO> users = userService.getUsersByProjectName(projectName);
        return ResponseEntity.ok(users);
    }
}