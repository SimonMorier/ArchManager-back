package com.archmanager_back.service.project;

import com.archmanager_back.config.constant.AppProperties;
import com.archmanager_back.context.UserProjectRegistry;
import com.archmanager_back.model.domain.ContainerInstance;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.dto.ProjectDTO;
import com.archmanager_back.model.entity.jpa.Permission;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.model.entity.jpa.User;
import com.archmanager_back.repository.jpa.PermissionRepository;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.repository.jpa.UserRepository;
import com.archmanager_back.util.LogUtils;
import com.archmanager_back.validator.PermissionValidator;
import com.archmanager_back.validator.ProjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.archmanager_back.config.constant.ErrorLabel.*;

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
    private final DockerProjectService dockerService;

    @Transactional
    public Project createAndStart(String name, String description, String username) throws InterruptedException {
        projectValidator.validateProjectName(name);

        String slug = Project.generateSlug(props.getProject().getSlugPrefix());
        String password = Project.generatePassword(props.getDocker().getPasswordLength());
        String volumeName = Project.generateVolumeName(slug, props.getProject().getVolumeSuffix());

        ContainerInstance inst = dockerService.provisionContainer(slug, password, volumeName);

        Project p = new Project();
        p.setName(name);
        p.setDescription(description);
        p.setSlug(slug);
        p.setContainerId(inst.containerId());
        p.setBoltPort(inst.hostBoltPort());
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

        dockerService.ensureProjectRunning(project);

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
        // vérification admin
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
            dockerService.stopContainer(proj.getContainerId());
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

}
