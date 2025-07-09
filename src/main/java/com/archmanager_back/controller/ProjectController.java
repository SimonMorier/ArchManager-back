package com.archmanager_back.controller;

import com.archmanager_back.context.SessionNeo4jContext;
import com.archmanager_back.model.dto.ProjectDTO;
import com.archmanager_back.model.entity.Project;
import com.archmanager_back.service.ProjectProvisioningService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectProvisioningService provisioning;
    private final SessionNeo4jContext sessionNeo4jContext;

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject() {
        try {
            Project p = provisioning.createAndStart("New Project");
            return ResponseEntity
                    .ok(new ProjectDTO(p.getSlug(), "bolt://localhost:" + p.getBoltPort()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/connect/{slug}")
    public ResponseEntity<ProjectDTO> connectProject(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session) {
        try {
            Project p = provisioning.connectProject(slug, userDetails.getUsername());
            // 1) on stocke l’ID du projet en session
            session.setAttribute("currentProjectId", p.getId());

            // 2) on force la ré-initialisation du Driver dans SessionNeo4jContext
            // (fermeture de l’ancien, création du nouveau)
            sessionNeo4jContext.getDriver();

            // 3) on renvoie l’URL à l’appelant
            ProjectDTO dto = new ProjectDTO(
                    p.getSlug(),
                    sessionNeo4jContext.getUri() // ou "bolt://localhost:" + p.getBoltPort()
            );
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
}
