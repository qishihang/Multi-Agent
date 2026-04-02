package com.qsh.multiagent.api.response;

import com.qsh.multiagent.domain.project.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {

    private String projectId;
    private ProjectStatus status;
    private String workspacePath;
}
