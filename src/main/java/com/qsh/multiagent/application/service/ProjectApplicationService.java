package com.qsh.multiagent.application.service;

import com.qsh.multiagent.domain.project.Project;

public interface ProjectApplicationService {

    Project createProject();

    Project getProject(String projectId);
}
