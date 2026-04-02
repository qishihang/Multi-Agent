package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.domain.project.Project;
import com.qsh.multiagent.domain.project.ProjectStatus;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultProjectApplicationService implements ProjectApplicationService {

    private final WorkspaceManager workspaceManager;

    public DefaultProjectApplicationService(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Project createProject() {
        Project project = new Project();
        project.setId(UUID.randomUUID().toString());
        project.setStatus(ProjectStatus.CREATED);

        workspaceManager.createWorkspaceForProject(project);
        project.setStatus(ProjectStatus.ACTIVE);
        return project;
    }

    @Override
    public Project getProject(String projectId) {
        return workspaceManager.getProjectOrThrow(projectId);
    }
}
