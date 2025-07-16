package com.archmanager_back.controller;

import com.archmanager_back.model.dto.PermissionDTO;
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

        /**
         * POST /api/permissions
         * Body JSON { projectSlug, username, role }
         * Seuls les ADMIN du projet peuvent accorder un droit.
         */
        @PostMapping
        @Transactional
        public ResponseEntity<PermissionRequestDTO> grantPermission(
                        @AuthenticationPrincipal UserDetails currentUser,
                        @Valid @RequestBody PermissionRequestDTO req) {
                PermissionRequestDTO resp = permissionService.grantPermission(currentUser.getUsername(), req);
                return ResponseEntity.ok(resp);
        }
}
