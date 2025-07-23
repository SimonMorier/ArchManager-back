package com.archmanager_back.controller;

import com.archmanager_back.model.dto.PermissionRequestDTO;
import com.archmanager_back.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

        private final PermissionService permissionService;

        @PostMapping
        @Transactional
        public ResponseEntity<PermissionRequestDTO> grantPermission(
                        @AuthenticationPrincipal UserDetails currentUser,
                        @Valid @RequestBody PermissionRequestDTO req) {
                PermissionRequestDTO resp = permissionService.grantPermission(currentUser.getUsername(), req);
                return ResponseEntity.ok(resp);
        }

        @DeleteMapping
        @Transactional
        public ResponseEntity<Void> revokePermission(
                        @AuthenticationPrincipal UserDetails currentUser,
                        @Valid @RequestBody PermissionRequestDTO req) {
                permissionService.revokePermission(currentUser.getUsername(), req);
                return ResponseEntity.noContent().build();
        }
}
