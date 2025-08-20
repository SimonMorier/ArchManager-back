package com.archmanager_back.api.user;

import com.archmanager_back.api.user.dto.AuthResponseDTO;
import com.archmanager_back.api.user.dto.UserRequestDTO;
import com.archmanager_back.api.user.dto.UserResponseDTO;
import com.archmanager_back.application.user.UserService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
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

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String username,
            Principal principal) {
        if (!principal.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String username,
            @RequestBody UserRequestDTO request,
            Principal principal) {
        if (!principal.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserResponseDTO updated = userService.updateUser(username, request);
        return ResponseEntity.ok(updated);
    }
}