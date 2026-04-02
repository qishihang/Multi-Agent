package com.qsh.multiagent.infrastructure.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SandboxContext {

    private String conversationId;
    private String projectId;
    private String workspaceRoot;
    private List<String> allowedWriteRoots;
    private List<String> allowedCommands;
}
