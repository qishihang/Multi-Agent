package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.domain.project.Project;
import com.qsh.multiagent.domain.project.ProjectStatus;
import com.qsh.multiagent.infrastructure.project.ProjectRegistry;
import com.qsh.multiagent.infrastructure.workspace.resolver.WorkspaceResolver;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultProjectApplicationService implements ProjectApplicationService {

    private final WorkspaceResolver workspaceResolver;
    private final ProjectRegistry projectRegistry;

    public DefaultProjectApplicationService(WorkspaceResolver workspaceResolver,
                                            ProjectRegistry projectRegistry) {
        this.workspaceResolver = workspaceResolver;
        this.projectRegistry = projectRegistry;
    }

    @Override
    public Project createProject() {
        Project project = new Project();
        project.setId(UUID.randomUUID().toString());
        project.setStatus(ProjectStatus.CREATED);

        workspaceResolver.createWorkspaceForProject(project);
        project.setStatus(ProjectStatus.ACTIVE);
        return project;
    }

    @Override
    public Project getProject(String projectId) {
        return projectRegistry.getProjectOrThrow(projectId);
    }
}
