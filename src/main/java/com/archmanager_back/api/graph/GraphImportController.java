package com.archmanager_back.api.graph;

import com.archmanager_back.api.graph.dto.GraphDTO;
import com.archmanager_back.api.graph.dto.update.GraphPatchDTO;
import com.archmanager_back.application.graph.GraphImportService;
import com.archmanager_back.application.graph.GraphMutationService;
import com.archmanager_back.application.project.ProjectService;
import com.archmanager_back.domain.project.Project;
import com.archmanager_back.domain.user.RoleEnum;
import com.archmanager_back.infrastructure.runtime.UserProjectRegistry;
import com.archmanager_back.shared.validator.GraphDTOValidator;
import com.archmanager_back.shared.validator.PermissionValidator;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/graph")
@RequiredArgsConstructor
public class GraphImportController {

        private final GraphImportService importSvc;
        private final ProjectService projectSvc;
        private final PermissionValidator permVal;
        private final GraphDTOValidator validator;
        private final UserProjectRegistry userProj;
        private final GraphMutationService mutationService;

        @PostMapping
        public ResponseEntity<ImportResponseDTO> importGraph(@AuthenticationPrincipal UserDetails user,
                        @Valid @RequestBody GraphDTO payload,
                        Errors errors) {

                Long projectId = userProj.currentProjectId(user.getUsername());
                Project project = projectSvc.findById(projectId)
                                .orElseThrow(() -> new IllegalStateException("Project not found"));

                permVal.requirePermission(user, project, RoleEnum.EDIT);
                validator.format(payload);
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

                importSvc.importGraph(user.getUsername(), payload);

                return ResponseEntity.ok(
                                ImportResponseDTO.builder()
                                                .success(true)
                                                .message("Graph imported successfully.")
                                                .projectSlug(project.getSlug())
                                                .build());
        }

        @DeleteMapping
        public ResponseEntity<Void> deleteGraph(
                        @AuthenticationPrincipal UserDetails user) {

                Long projectId = userProj.currentProjectId(user.getUsername());
                Project project = projectSvc.findById(projectId)
                                .orElseThrow(() -> new IllegalStateException("Project not found"));

                permVal.requirePermission(user, project, RoleEnum.EDIT);
                importSvc.deleteGraph(user.getUsername());

                return ResponseEntity.noContent().build();
        }

        @PostMapping("/mutate")
        @Transactional
        public ResponseEntity<GraphDTO> mutate(
                        @AuthenticationPrincipal UserDetails user,
                        @RequestBody GraphPatchDTO patch) {
                Long projectId = userProj.currentProjectId(user.getUsername());
                Project project = projectSvc.findById(projectId)
                                .orElseThrow(() -> new IllegalStateException("Project not found"));

                permVal.requirePermission(user, project, RoleEnum.EDIT);
                GraphDTO updated = mutationService.mutateAndReimport(user.getUsername(), patch);
                return ResponseEntity.ok(updated);
        }
}
