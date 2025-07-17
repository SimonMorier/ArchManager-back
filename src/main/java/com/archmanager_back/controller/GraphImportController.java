// src/main/java/com/archmanager_back/controller/GraphImportController.java
package com.archmanager_back.controller;

import com.archmanager_back.model.dto.ImportResponseDTO;
import com.archmanager_back.model.dto.graph.GraphDTO;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.context.UserProjectRegistry;
import com.archmanager_back.service.GraphImportService;
import com.archmanager_back.service.ProjectService;
import com.archmanager_back.validator.GraphDTOValidator;
import com.archmanager_back.validator.PermissionValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/graph") // plus de {slug}
@RequiredArgsConstructor
public class GraphImportController {

        private final GraphImportService importSvc;
        private final ProjectService projectSvc;
        private final PermissionValidator permVal;
        private final GraphDTOValidator validator;
        private final UserProjectRegistry userProj;

        @PostMapping
        public ResponseEntity<ImportResponseDTO> importGraph(@AuthenticationPrincipal UserDetails user,
                        @Valid @RequestBody GraphDTO payload,
                        Errors errors) {

                /* 1️⃣ Récupère le projet actif de l'utilisateur */
                Long projectId = userProj.currentProjectId(user.getUsername());
                Project project = projectSvc.findById(projectId)
                                .orElseThrow(() -> new IllegalStateException("Project not found"));

                /* 2️⃣ Vérifie les droits */
                permVal.requirePermission(user, project, RoleEnum.EDIT);

                /* 3️⃣ Valide le payload */
                validator.validate(payload, errors);
                if (errors.hasErrors()) {
                        String msg = errors.getAllErrors().stream()
                                        .map(e -> e.getDefaultMessage())
                                        .reduce((a, b) -> a + "; " + b).orElse("Invalid graph payload");

                        return ResponseEntity.badRequest().body(
                                        ImportResponseDTO.builder()
                                                        .success(false)
                                                        .message(msg)
                                                        .projectSlug(project.getSlug())
                                                        .build());
                }

                /* 4️⃣ Import */
                importSvc.importGraph(user.getUsername(), payload);

                /* 5️⃣ Réponse */
                return ResponseEntity.ok(
                                ImportResponseDTO.builder()
                                                .success(true)
                                                .message("Graph imported successfully.")
                                                .projectSlug(project.getSlug())
                                                .build());
        }
}
