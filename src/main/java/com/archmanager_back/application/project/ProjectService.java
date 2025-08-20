package com.archmanager_back.application.project;

import com.archmanager_back.api.project.dto.ProjectDTO;
import com.archmanager_back.domain.project.Permission;
import com.archmanager_back.domain.project.Project;
import com.archmanager_back.domain.user.RoleEnum;
import com.archmanager_back.domain.user.User;
import com.archmanager_back.infrastructure.config.constant.AppProperties;
import com.archmanager_back.infrastructure.graph.spi.GraphProjectProvisioner;
import com.archmanager_back.infrastructure.persistence.jpa.PermissionRepository;
import com.archmanager_back.infrastructure.persistence.jpa.ProjectRepository;
import com.archmanager_back.infrastructure.persistence.jpa.UserRepository;
import com.archmanager_back.infrastructure.runtime.UserProjectRegistry;
import com.archmanager_back.shared.util.LogUtils;
import com.archmanager_back.shared.validator.PermissionValidator;
import com.archmanager_back.shared.validator.ProjectValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.archmanager_back.infrastructure.config.constant.ErrorLabel.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final UserProjectRegistry userProjectRegistry;
    private final AppProperties props;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final PermissionRepository permissionRepo;
    private final ProjectValidator projectValidator;
    private final PermissionValidator permissionValidator;
    private final GraphProjectProvisioner graphProvisioner;

    @Transactional
    public Project createAndStart(String name, String description, String username) throws InterruptedException {
        projectValidator.validateProjectName(name);

        String prefix = props.getProject().getSlugPrefix();
        String slug;
        do {
            slug = Project.generateSlug(prefix);
        } while (projectRepo.existsBySlug(slug));
        String password = Project.generatePassword(props.getDocker().getPasswordLength());
        String volumeName = Project.generateVolumeName(slug, props.getProject().getVolumeSuffix());

        var conn = graphProvisioner.provisionProject(slug, password, volumeName);

        Project p = new Project();
        p.setName(name);
        p.setDescription(description);
        p.setSlug(slug);
        p.setContainerId(conn.getContainerId());
        p.setBoltPort(conn.getBoltPort());
        p.setVolumeName(volumeName);
        p.setPassword(password);
        p.incrementSessions();

        User creator = userRepo.findByUsernameWithPermissions(username)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
        Permission perm = new Permission(creator, p, RoleEnum.ADMIN);
        p.addPermission(perm);
        creator.addPermission(perm);

        projectRepo.save(p);
        userRepo.save(creator);

        return p;
    }

    @Transactional
    public ProjectDTO connectProject(String slug, String username) throws InterruptedException {
        projectValidator.validateConnectParams(slug, username);

        User user = userRepo.findByUsernameWithPermissions(username)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
        Project project = projectRepo.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException(PROJECT_NOT_FOUND));

        try {
            Long previousProjectId = userProjectRegistry.currentProjectId(username);
            if (!previousProjectId.equals(project.getId())) {
                log.debug("User {} was on project {}, disconnecting it first", username, previousProjectId);
                disconnectProject(previousProjectId);
            }
        } catch (IllegalStateException e) {
        }

        permissionValidator.requirePermission(user, project, RoleEnum.READ);

        graphProvisioner.ensureProjectAvailable(project);

        project.incrementSessions();
        log.debug(LogUtils.userPrefixed(" Connected to project {}"), project.getSlug());
        projectRepo.save(project);

        String boltUri = props.getNeo4j().getBoltPrefix() + props.getDocker().getHost() + project.getBoltPort();

        return ProjectDTO.from(project, boltUri);
    }

    @Transactional
    public void disconnectProject(Long projectId) {
        Project p = projectRepo.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + projectId));
        p.decrementSessions();
        projectRepo.save(p);
        log.debug("Project {} sessions decremented, now {}", p.getSlug(), p.getActiveSessionCount());
    }

    public ProjectDTO createProject(String name, String description) throws InterruptedException {
        String username = getCurrentUsername();
        Project p = createAndStart(name, description, username);
        String boltUri = props.getNeo4j().getBoltPrefix()
                + props.getDocker().getHost()
                + p.getBoltPort();
        return ProjectDTO.from(p, boltUri);
    }

    public Optional<Project> findBySlug(String slug) {
        return projectRepo.findBySlug(slug);
    }

    public Optional<Project> findById(Long id) {
        return projectRepo.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        throw new IllegalStateException("No authenticated user found in security context");
    }

    @Transactional
    public ProjectDTO updateProject(String slug, String newName, String newDescription, String username) {
        Project proj = projectRepo.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + slug));
        proj.setName(newName);
        proj.setDescription(newDescription);
        Project saved = projectRepo.save(proj);
        String boltUri = props.getNeo4j().getBoltPrefix()
                + props.getDocker().getHost()
                + saved.getBoltPort();
        return ProjectDTO.from(saved, boltUri);
    }

    @Transactional
    public void deleteProject(String slug, String username) throws InterruptedException {
        Project proj = projectRepo.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + slug));

        boolean isAdmin = permissionRepo
                .existsByProject_SlugAndUser_UsernameAndRole(slug, username, RoleEnum.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException("Not admin of project " + slug);
        }

        try {
            graphProvisioner.stopProject(proj);
        } catch (Exception e) {
            log.warn("Failed to stop container {}: {}", proj.getContainerId(), e.getMessage());
        }

        projectRepo.delete(proj);
        log.info("Project {} deleted by user {}", slug, username);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsForUser(String username) {
        List<Project> projects = projectRepo.findAllByPermissions_User_Username(username);

        return projects.stream()
                .map(p -> {
                    String boltUri = props.getNeo4j().getBoltPrefix()
                            + props.getDocker().getHost()
                            + p.getBoltPort();
                    return ProjectDTO.from(p, boltUri);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectBySlugForUser(String slug, String username) {
        Project project = projectRepo.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Unknown project: " + slug));

        boolean hasPermission = project.getPermissions().stream()
                .anyMatch(perm -> perm.getUser().getUsername().equals(username));
        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission to access this project");
        }

        String boltUri = props.getNeo4j().getBoltPrefix()
                + props.getDocker().getHost()
                + project.getBoltPort();

        return ProjectDTO.from(project, boltUri);
    }

}
