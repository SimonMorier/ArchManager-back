package com.archmanager_back.api.project;

import com.archmanager_back.api.project.dto.ProjectDTO;
import com.archmanager_back.api.project.dto.ProjectPermissionDTO;
import com.archmanager_back.api.project.dto.ProjectRequestDTO;
import com.archmanager_back.api.user.dto.UserResponseDTO;
import com.archmanager_back.application.permission.PermissionService;
import com.archmanager_back.application.project.ProjectService;
import com.archmanager_back.infrastructure.runtime.UserProjectRegistry;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserProjectRegistry userProjectRegistry;
    private final PermissionService permissionService;

    @PostMapping("/create")
    public ResponseEntity<ProjectDTO> createProject(
            @RequestBody ProjectRequestDTO request)
            throws InterruptedException {
        ProjectDTO dto = projectService.createProject(request.getName(), request.getDescription());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{slug}/connect")
    public ResponseEntity<ProjectDTO> connectProject(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) throws InterruptedException {

        ProjectDTO dto = projectService.connectProject(slug, user.getUsername());
        Long projectId = projectService.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Unknown project: " + slug))
                .getId();
        userProjectRegistry.connect(user.getUsername(), projectId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{slug}/disconnect")
    public ResponseEntity<Void> disconnectProject(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) {

        Long projectId = projectService.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Unknown project: " + slug))
                .getId();

        projectService.disconnectProject(projectId);
        userProjectRegistry.disconnect(user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{slug}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable String slug,
            @RequestBody ProjectRequestDTO request,
            @AuthenticationPrincipal UserDetails user)
            throws InterruptedException {
        ProjectDTO updated = projectService.updateProject(
                slug, request.getName(), request.getDescription(), user.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user)
            throws InterruptedException {

        projectService.deleteProject(slug, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> listUserProjects(
            @AuthenticationPrincipal UserDetails user) {
        List<ProjectDTO> dtos = projectService.getProjectsForUser(user.getUsername());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProjectDTO> getProjectBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) {

        ProjectDTO dto = projectService.getProjectBySlugForUser(slug, user.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{slug}/permissions")
    public ResponseEntity<List<ProjectPermissionDTO>> listProjectPermissions(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                permissionService.listProjectPermissions(user.getUsername(), slug));
    }

    @GetMapping("/{slug}/users-without-permission")
    public ResponseEntity<List<UserResponseDTO>> listUsersWithoutPermission(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) {

        List<UserResponseDTO> users = permissionService
                .listUsersWithoutPermissionForProject(user.getUsername(), slug);

        return ResponseEntity.ok(users);
    }
}
