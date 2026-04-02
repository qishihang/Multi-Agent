package com.qsh.multiagent.domain.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    private String id;
    private ProjectStatus status;
    private String workspacePath;
}
