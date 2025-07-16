package com.archmanager_back.controller;

import com.archmanager_back.context.SessionNeo4jContext;
import com.archmanager_back.model.dto.ProjectDTO;
import com.archmanager_back.service.ProjectService;

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

    private final ProjectService projectService;
    private final SessionNeo4jContext sessionNeo4jContext;

    @PostMapping("/{name}")
    public ResponseEntity<ProjectDTO> createProject(@PathVariable("name") String name) {
        try {
            ProjectDTO dto = projectService.createProject(name);
            return ResponseEntity.ok(dto);
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
            ProjectDTO dto = projectService.connectProject(slug, userDetails.getUsername(), session,
                    sessionNeo4jContext);
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

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnectProject(HttpSession session) {
        projectService.disconnectProject(session, sessionNeo4jContext);
        return ResponseEntity.noContent().build();
    }
}
