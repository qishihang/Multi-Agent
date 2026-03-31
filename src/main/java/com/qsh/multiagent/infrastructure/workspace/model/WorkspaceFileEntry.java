package com.qsh.multiagent.infrastructure.workspace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceFileEntry {

    private String relativePath;
    private boolean directory;
}
