// src/main/java/com/archmanager_back/controller/GraphImportController.java
package com.archmanager_back.controller;

import com.archmanager_back.model.dto.ImportResponseDTO;
import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.model.entity.jpa.User;
import com.archmanager_back.service.GraphImportService;
import com.archmanager_back.service.ProjectService;
import com.archmanager_back.validator.GraphDTOValidator;
import com.archmanager_back.validator.PermissionValidator;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/upload/graph")
@RequiredArgsConstructor
public class GraphImportController {

        private final GraphImportService importService;
        private final ProjectService projectService;
        private final PermissionValidator permissionValidator;
        private final GraphDTOValidator graphDTOValidator;
        private final HttpSession httpSession;

        @PostMapping
        public ResponseEntity<ImportResponseDTO> importGraph(
                        @AuthenticationPrincipal UserDetails user,
                        @Valid @RequestBody GraphDTO payload,
                        Errors errors) {

                Long projectId = (Long) httpSession.getAttribute("currentProjectId");
                if (projectId == null) {
                        return ResponseEntity.badRequest().body(
                                        ImportResponseDTO.builder()
                                                        .success(false)
                                                        .message("No project selected in session.")
                                                        .projectSlug(null)
                                                        .build());
                }

                Project proj = projectService.findById(projectId)
                                .orElseThrow(() -> new IllegalArgumentException("Unknown project in session"));
                User currentUser = projectService.findUserByUsername(user.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("Unknown user"));
                permissionValidator.requirePermission(currentUser, proj, RoleEnum.READ);

                graphDTOValidator.validate(payload, errors);
                if (errors.hasErrors()) {
                        String msg = errors.getAllErrors().stream()
                                        .map(e -> e.getDefaultMessage())
                                        .reduce((a, b) -> a + "; " + b)
                                        .orElse("Invalid graph payload");
                        return ResponseEntity.badRequest().body(
                                        ImportResponseDTO.builder()
                                                        .success(false)
                                                        .message(msg)
                                                        .projectSlug(proj.getSlug())
                                                        .build());
                }

                importService.importGraph(payload);

                return ResponseEntity.ok(
                                ImportResponseDTO.builder()
                                                .success(true)
                                                .message("Graph imported successfully.")
                                                .projectSlug(proj.getSlug())
                                                .build());
        }
}
