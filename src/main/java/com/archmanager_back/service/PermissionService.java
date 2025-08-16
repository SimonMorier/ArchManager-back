package com.archmanager_back.service;

import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.dto.PermissionRequestDTO;
import com.archmanager_back.model.entity.jpa.Permission;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.model.entity.jpa.User;
import com.archmanager_back.repository.jpa.PermissionRepository;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.repository.jpa.UserRepository;
import com.archmanager_back.validator.PermissionValidator;

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

        Permission perm = new Permission(target, project, req.getRole());
        project.addPermission(perm);
        projectRepo.save(project);

        PermissionRequestDTO dto = new PermissionRequestDTO();
        dto.setUsername(target.getUsername());
        dto.setProjectSlug(perm.getProject().getSlug());
        dto.setRole(perm.getRole());
        return dto;
    }

    public void revokePermission(String adminUsername, PermissionRequestDTO req) {
        boolean isAdmin = permissionRepo.existsByProjectSlugAndUsernameAndRole(
                req.getProjectSlug(), adminUsername, RoleEnum.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException("Only ADMIN can revoke permissions");
        }

        Permission perm = permissionRepo
                .findByProjectSlugAndUsernameAndRole(
                        req.getProjectSlug(), req.getUsername(), req.getRole())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Permission introuvable pour " + req.getUsername()));
        permissionRepo.delete(perm);
    }

    @Transactional(readOnly = true)
    public List<PermissionRequestDTO> getUserPermissions(String username) {
        // On récupère toutes les Permission pour cet utilisateur
        List<Permission> perms = permissionRepo.findAllByUser_Username(username);

        // On mappe en PermissionRequestDTO (username, projectSlug, role)
        return perms.stream()
                .map(p -> new PermissionRequestDTO(
                        p.getProject().getSlug(),
                        p.getUser().getUsername(),
                        p.getRole()))
                .toList();
    }
}