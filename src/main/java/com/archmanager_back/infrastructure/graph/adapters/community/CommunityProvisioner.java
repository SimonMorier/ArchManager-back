package com.archmanager_back.infrastructure.graph.adapters.community;

import com.archmanager_back.domain.project.Project;
import com.archmanager_back.infrastructure.docker.DockerProjectService;
import com.archmanager_back.infrastructure.graph.spi.GraphProjectProvisioner;
import com.archmanager_back.infrastructure.graph.spi.ProjectConnection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityProvisioner implements GraphProjectProvisioner {

    private final DockerProjectService docker;

    @Override
    public ProjectConnection provisionProject(String slug, String password, String volumeName)
            throws InterruptedException {
        var inst = docker.provisionContainer(slug, password, volumeName);
        return new ProjectConnection(inst.containerId(), inst.hostBoltPort(), null);
    }

    @Override
    public void ensureProjectAvailable(Project project) throws InterruptedException {
        docker.ensureProjectRunning(project);
    }

    @Override
    public void stopProject(Project project) {
        if (project.getContainerId() != null) {
            docker.stopContainer(project.getContainerId());
        }
    }

    @Override
    public void destroyProject(Project project) {
        if (project.getContainerId() != null) {
            docker.stopContainer(project.getContainerId());
        }
    }
}
