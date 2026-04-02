package com.qsh.multiagent.api.controller;

import com.qsh.multiagent.api.response.ProjectResponse;
import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.domain.project.Project;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectApplicationService projectApplicationService;

    public ProjectController(ProjectApplicationService projectApplicationService) {
        this.projectApplicationService = projectApplicationService;
    }

    @PostMapping
    public ProjectResponse createProject() {
        Project project = projectApplicationService.createProject();
        return new ProjectResponse(
                project.getId(),
                project.getStatus(),
                project.getWorkspacePath()
        );
    }
}
