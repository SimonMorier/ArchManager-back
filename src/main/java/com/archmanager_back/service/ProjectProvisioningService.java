package com.archmanager_back.service;

import com.archmanager_back.config.constant.AppProperties;
import com.archmanager_back.model.domain.ContainerInstance;
import com.archmanager_back.model.entity.*;
import com.archmanager_back.repository.jpa.ProjectRepository;
import com.archmanager_back.repository.jpa.UserRepository;
import com.archmanager_back.validator.PermissionValidator;
import com.archmanager_back.validator.ProjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static com.archmanager_back.config.constant.ErrorLabel.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectProvisioningService {

    private final AppProperties props;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final ProjectValidator projectValidator;
    private final PermissionValidator permissionValidator;
    private final DockerProjectService dockerService;

    @Transactional
    public Project createAndStart(String name) throws InterruptedException {
        projectValidator.validateProjectName(name);

        String slug = Project.generateSlug(props.getProject().getSlugPrefix());
        String password = Project.generatePassword(props.getDocker().getPasswordLength());
        String volumeName = Project.generateVolumeName(slug, props.getProject().getVolumeSuffix());

        // delegate all Docker work here:
        ContainerInstance inst = dockerService.provisionContainer(slug, password, volumeName);

        Project p = new Project();
        p.setName(name);
        p.setSlug(slug);
        p.setContainerId(inst.containerId());
        p.setBoltPort(inst.hostBoltPort());
        p.setVolumeName(volumeName);
        p.setPassword(password);
        return projectRepo.save(p);
    }

    @Transactional
    public Project connectProject(String slug, String username) throws InterruptedException {
        projectValidator.validateConnectParams(slug, username);

        User user = userRepo.findByUsernameWithPermissions(username)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
        Project project = projectRepo.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException(PROJECT_NOT_FOUND));

        permissionValidator.requirePermission(user, project, Role.READ);

        // delegate container start/recreate
        dockerService.ensureProjectRunning(project);

        return project;
    }
}
