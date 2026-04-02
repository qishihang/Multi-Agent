package com.qsh.multiagent.infrastructure.workspace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceContext {

    private String projectId;
    private String rootPath;
    private boolean available;
}
