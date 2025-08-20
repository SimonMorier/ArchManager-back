package com.archmanager_back.application.permission;

import com.archmanager_back.api.permission.dto.PermissionRequestDTO;
import com.archmanager_back.api.project.dto.ProjectPermissionDTO;
import com.archmanager_back.api.user.dto.UserResponseDTO;
import com.archmanager_back.domain.project.Permission;
import com.archmanager_back.domain.project.Project;
import com.archmanager_back.domain.user.RoleEnum;
import com.archmanager_back.domain.user.User;
import com.archmanager_back.infrastructure.persistence.jpa.PermissionRepository;
import com.archmanager_back.infrastructure.persistence.jpa.ProjectRepository;
import com.archmanager_back.infrastructure.persistence.jpa.UserRepository;
import com.archmanager_back.shared.validator.PermissionValidator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final PermissionRepository permissionRepo;
    private final PermissionValidator permissionValidator;

    private User getUserWithPermissions(String username) {
        return userRepo.findByUsernameWithPermissions(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private Project getProjectBySlug(String slug) {
        return projectRepo.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + slug));
    }

    @Transactional
    public PermissionRequestDTO grantPermission(String callerUsername, PermissionRequestDTO req) {
        Project project = getProjectBySlug(req.getProjectSlug());
        User caller = getUserWithPermissions(callerUsername);
        permissionValidator.requirePermission(caller, project, RoleEnum.ADMIN);

        User target = getUserWithPermissions(req.getUsername());

        Permission perm = permissionRepo
                .findByProject_SlugAndUser_Username(project.getSlug(), target.getUsername())
                .orElse(null);

        if (perm == null) {
            perm = new Permission(target, project, req.getRole());
            project.addPermission(perm);
        } else {
            perm.setRole(req.getRole());
        }

        permissionRepo.save(perm);

        PermissionRequestDTO dto = new PermissionRequestDTO();
        dto.setProjectSlug(project.getSlug());
        dto.setUsername(target.getUsername());
        dto.setRole(perm.getRole());
        return dto;
    }

    public void revokePermission(String adminUsername, PermissionRequestDTO req) {
        boolean isAdmin = permissionRepo.existsByProject_SlugAndUser_UsernameAndRole(
                req.getProjectSlug(), adminUsername, RoleEnum.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException("Only ADMIN can revoke permissions");
        }

        Permission perm = permissionRepo
                .findByProject_SlugAndUser_UsernameAndRole(
                        req.getProjectSlug(), req.getUsername(), req.getRole())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Permission introuvable pour " + req.getUsername()));
        permissionRepo.delete(perm);
    }

    @Transactional(readOnly = true)
    public List<PermissionRequestDTO> getUserPermissions(String username) {
        List<Permission> perms = permissionRepo.findAllByUser_Username(username);

        return perms.stream()
                .map(p -> new PermissionRequestDTO(
                        p.getProject().getSlug(),
                        p.getUser().getUsername(),
                        p.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectPermissionDTO> listProjectPermissions(String callerUsername, String projectSlug) {
        Project project = getProjectBySlug(projectSlug);
        User caller = getUserWithPermissions(callerUsername);
        permissionValidator.requirePermission(caller, project, RoleEnum.READ);

        List<Permission> perms = permissionRepo.findAllByProject_Slug(projectSlug);

        return perms.stream()
                .map(p -> new ProjectPermissionDTO(
                        p.getProject().getSlug(),
                        p.getUser().getUsername(),
                        p.getUser().getFirstname(),
                        p.getUser().getLastname(),
                        p.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listUsersWithoutPermissionForProject(String callerUsername, String projectSlug) {
        Project project = getProjectBySlug(projectSlug);
        User caller = getUserWithPermissions(callerUsername);
        permissionValidator.requirePermission(caller, project, RoleEnum.READ);

        return userRepo.findAllWithoutPermissionForProject(projectSlug).stream()
                .map(u -> new UserResponseDTO(u.getId(), u.getUsername(), u.getFirstname(), u.getLastname()))
                .toList();
    }
}