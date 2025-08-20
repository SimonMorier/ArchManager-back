package com.archmanager_back.infrastructure.graph.spi;

import com.archmanager_back.domain.project.Project;

public interface GraphProjectProvisioner {
    ProjectConnection provisionProject(String slug, String password, String volumeName) throws InterruptedException;

    void ensureProjectAvailable(Project project) throws InterruptedException;

    void stopProject(Project project);

    void destroyProject(Project project);
}
