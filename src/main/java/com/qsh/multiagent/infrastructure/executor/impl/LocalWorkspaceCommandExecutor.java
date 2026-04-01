package com.qsh.multiagent.infrastructure.executor.impl;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.executor.WorkspaceCommandExecutor;
import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocalWorkspaceCommandExecutor implements WorkspaceCommandExecutor { // 本地工作区执行器

    private final WorkspaceManager workspaceManager;

    public LocalWorkspaceCommandExecutor(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public CommandExecutionResult execute(Conversation conversation, List<String> command) {
        Path root = workspaceManager.getWorkspaceRoot(conversation);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(root.toFile());

        try {
            Process process = processBuilder.start();

            String stdout;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                stdout = reader.lines().collect(Collectors.joining("\n"));
            }

            String stderr;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                stderr = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();

            return new CommandExecutionResult(
                    exitCode == 0,
                    exitCode,
                    stdout,
                    stderr
            );
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to execute command in workspace: " + command, e);
        }
    }
}
