package com.qsh.multiagent.infrastructure.project;

import com.qsh.multiagent.domain.project.Project;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProjectRegistry {

    private final Map<String, Project> projects = new ConcurrentHashMap<>();

    public void register(Project project) {
        if (project == null || project.getId() == null || project.getId().isBlank()) {
            throw new IllegalArgumentException("Project id must not be blank");
        }
        projects.put(project.getId(), project);
    }

    public Project getProjectOrThrow(String projectId) {
        Project project = projects.get(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return project;
    }
}
