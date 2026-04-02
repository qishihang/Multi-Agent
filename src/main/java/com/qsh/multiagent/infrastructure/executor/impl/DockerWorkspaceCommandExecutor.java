package com.qsh.multiagent.infrastructure.executor.impl;

import com.qsh.multiagent.infrastructure.executor.WorkspaceCommandExecutor;
import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.sandbox.model.SandboxContext;
import com.qsh.multiagent.infrastructure.sandbox.model.SandboxSession;
import com.qsh.multiagent.infrastructure.sandbox.policy.SandboxPolicy;
import com.qsh.multiagent.infrastructure.sandbox.service.ConversationSandboxManager;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DockerWorkspaceCommandExecutor implements WorkspaceCommandExecutor {

    private static final long DEFAULT_TIMEOUT_SECONDS = 120;
    private static final long PREPARATION_TIMEOUT_SECONDS = 600;
    private static final long BUILD_TIMEOUT_SECONDS = 300;

    private final SandboxPolicy sandboxPolicy;
    private final ConversationSandboxManager conversationSandboxManager;

    public DockerWorkspaceCommandExecutor(SandboxPolicy sandboxPolicy,
                                          ConversationSandboxManager conversationSandboxManager) {
        this.sandboxPolicy = sandboxPolicy;
        this.conversationSandboxManager = conversationSandboxManager;
    }

    @Override
    public CommandExecutionResult execute(SandboxContext sandboxContext, List<String> command) {
        sandboxPolicy.validateCommand(sandboxContext, command);

        Path workspaceRoot = Path.of(sandboxContext.getWorkspaceRoot()).toAbsolutePath().normalize();
        sandboxPolicy.validateAccessPath(sandboxContext, workspaceRoot);

        SandboxSession sandboxSession;
        try {
            sandboxSession = conversationSandboxManager.getOrCreateSession(sandboxContext);
        } catch (IllegalStateException e) {
            return new CommandExecutionResult(false, -1, "", e.getMessage());
        }

        List<String> dockerCommand = buildDockerExecCommand(sandboxSession, command);
        ProcessBuilder processBuilder = new ProcessBuilder(dockerCommand);
        long timeoutSeconds = resolveTimeoutSeconds(command);

        try {
            Process process = processBuilder.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CommandExecutionResult(
                        false,
                        -1,
                        "",
                        "Docker command timed out after " + timeoutSeconds + " seconds: " + String.join(" ", command)
                );
            }

            String stdout;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                stdout = reader.lines().collect(Collectors.joining("\n"));
            }

            String stderr;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                stderr = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.exitValue();

            return new CommandExecutionResult(
                    exitCode == 0,
                    exitCode,
                    truncate(stdout, 12000),
                    truncate(stderr, 12000)
            );
        } catch (IOException e) {
            return new CommandExecutionResult(false, -1, "", "Failed to execute docker command: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CommandExecutionResult(false, -1, "", "Docker command execution interrupted");
        }
    }

    private List<String> buildDockerExecCommand(SandboxSession sandboxSession, List<String> command) {
        List<String> dockerCommand = new java.util.ArrayList<>();
        dockerCommand.add("docker");
        dockerCommand.add("exec");
        dockerCommand.add(sandboxSession.getContainerName());
        dockerCommand.addAll(command);
        return dockerCommand;
    }

    private long resolveTimeoutSeconds(List<String> command) {
        String normalizedCommand = String.join(" ", command);

        if (normalizedCommand.contains("dependency:go-offline")
                || normalizedCommand.contains("npm install")
                || normalizedCommand.contains("pip install")
                || normalizedCommand.contains("go mod download")
                || normalizedCommand.contains("cargo fetch")
                || normalizedCommand.contains("gradle dependencies")
                || normalizedCommand.contains("./gradlew dependencies")) {
            return PREPARATION_TIMEOUT_SECONDS;
        }

        if (normalizedCommand.contains("test-compile")
                || normalizedCommand.contains("testClasses")
                || normalizedCommand.contains("cargo check")
                || normalizedCommand.contains("go build ./...")
                || normalizedCommand.contains("compileall")
                || normalizedCommand.contains("npm run build")) {
            return BUILD_TIMEOUT_SECONDS;
        }

        return DEFAULT_TIMEOUT_SECONDS;
    }

    private String truncate(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "\n... [truncated]";
    }
}
