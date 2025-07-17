package com.archmanager_back.service;

import com.archmanager_back.config.constant.AppProperties;
import com.archmanager_back.model.domain.ContainerInstance;
import com.archmanager_back.model.domain.RoleEnum;
import com.archmanager_back.model.dto.ProjectDTO;
import com.archmanager_back.model.entity.jpa.Permission;
import com.archmanager_back.model.entity.jpa.Project;
import com.archmanager_back.model.entity.jpa.User;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.repository.jpa.UserRepository;
import com.archmanager_back.util.LogUtils;
import com.archmanager_back.validator.PermissionValidator;
import com.archmanager_back.validator.ProjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

import static com.archmanager_back.config.constant.ErrorLabel.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final AppProperties props;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final ProjectValidator projectValidator;
    private final PermissionValidator permissionValidator;
    private final DockerProjectService dockerService;

    @Transactional
    public Project createAndStart(String name, String username) throws InterruptedException {
        projectValidator.validateProjectName(name);

        String slug = Project.generateSlug(props.getProject().getSlugPrefix());
        String password = Project.generatePassword(props.getDocker().getPasswordLength());
        String volumeName = Project.generateVolumeName(slug, props.getProject().getVolumeSuffix());

        ContainerInstance inst = dockerService.provisionContainer(slug, password, volumeName);

        Project p = new Project();
        p.setName(name);
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

        permissionValidator.requirePermission(user, project, RoleEnum.READ);

        dockerService.ensureProjectRunning(project);

        project.incrementSessions();
        log.debug(LogUtils.userPrefixed(" Connected to project {}"), project.getSlug());
        projectRepo.save(project);

        String boltUri = "bolt://localhost:" + project.getBoltPort();
        return new ProjectDTO(project.getSlug(), boltUri);
    }

    @Transactional
    public void disconnectProject(Long projectId) {
        Project p = projectRepo.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + projectId));
        p.decrementSessions();
        projectRepo.save(p);
        log.debug("Project {} sessions decremented, now {}", p.getSlug(), p.getActiveSessionCount());
    }

    public ProjectDTO createProject(String name) throws InterruptedException {
        String username = getCurrentUsername();
        Project p = createAndStart(name, username);
        return new ProjectDTO(p.getSlug(), "bolt://localhost:" + p.getBoltPort());
    }

    // public void disconnectProject(HttpSession session, SessionNeo4jContext ctx) {
    // Long projectId = (Long) session.getAttribute("currentProjectId");
    // if (projectId != null) {
    // disconnectProject(projectId);
    // session.removeAttribute("currentProjectId");
    // try {
    // if (ctx.getDriver() != null) {
    // ctx.getDriver().close();
    // }
    // } catch (Exception ignored) {
    // }
    // }
    // }

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
}
