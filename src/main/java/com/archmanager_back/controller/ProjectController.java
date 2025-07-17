package com.archmanager_back.controller;

import com.archmanager_back.context.UserProjectRegistry;
import com.archmanager_back.model.dto.ProjectDTO;
import com.archmanager_back.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserProjectRegistry userProjectRegistry;

    /* ------------ 1. Création -------------- */
    @PostMapping("/{name}")
    public ResponseEntity<ProjectDTO> createProject(@PathVariable String name) {
        try {
            return ResponseEntity.ok(projectService.createProject(name));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{slug}/connect")
    public ResponseEntity<ProjectDTO> connectProject(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) {
        try {
            ProjectDTO dto = projectService.connectProject(slug, user.getUsername());

            Long projectId = projectService.findBySlug(slug)
                    .orElseThrow(() -> new NoSuchElementException("Unknown project: " + slug))
                    .getId();

            userProjectRegistry.connect(user.getUsername(), projectId);

            return ResponseEntity.ok(dto);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (AccessDeniedException ade) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException nse) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /* ------------ 3. Déconnexion ------------ */
    @PostMapping("/{slug}/disconnect")
    public ResponseEntity<Void> disconnectProject(@PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) {

        Long projectId = projectService.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Unknown project: " + slug))
                .getId();

        projectService.disconnectProject(projectId);
        userProjectRegistry.disconnect(user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
