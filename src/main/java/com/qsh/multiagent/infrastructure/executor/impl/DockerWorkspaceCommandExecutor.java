package com.qsh.multiagent.infrastructure.executor.impl;

import com.qsh.multiagent.infrastructure.executor.WorkspaceCommandExecutor;
import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.sandbox.model.SandboxContext;
import com.qsh.multiagent.infrastructure.sandbox.policy.SandboxPolicy;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DockerWorkspaceCommandExecutor implements WorkspaceCommandExecutor {

    private static final long DEFAULT_TIMEOUT_SECONDS = 120;
    private static final String CONTAINER_WORKSPACE = "/workspace";

    private final SandboxPolicy sandboxPolicy;

    public DockerWorkspaceCommandExecutor(SandboxPolicy sandboxPolicy) {
        this.sandboxPolicy = sandboxPolicy;
    }

    @Override
    public CommandExecutionResult execute(SandboxContext sandboxContext, List<String> command) {
        sandboxPolicy.validateCommand(sandboxContext, command);

        Path workspaceRoot = Path.of(sandboxContext.getWorkspaceRoot()).toAbsolutePath().normalize();
        sandboxPolicy.validateAccessPath(sandboxContext, workspaceRoot);

        List<String> dockerCommand = buildDockerCommand(workspaceRoot, command);
        ProcessBuilder processBuilder = new ProcessBuilder(dockerCommand);

        try {
            Process process = processBuilder.start();

            boolean finished = process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CommandExecutionResult(
                        false,
                        -1,
                        "",
                        "Docker command timed out after " + DEFAULT_TIMEOUT_SECONDS + " seconds"
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
            throw new IllegalStateException("Failed to execute docker command: " + dockerCommand, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Docker command execution interrupted: " + dockerCommand, e);
        }
    }

    private List<String> buildDockerCommand(Path workspaceRoot, List<String> command) {
        List<String> dockerCommand = new ArrayList<>();
        dockerCommand.add("docker");
        dockerCommand.add("run");
        dockerCommand.add("--rm");
        dockerCommand.add("-v");
        dockerCommand.add(workspaceRoot + ":" + CONTAINER_WORKSPACE);
        dockerCommand.add("-w");
        dockerCommand.add(CONTAINER_WORKSPACE);
        dockerCommand.add(resolveImage(command));
        dockerCommand.addAll(command);
        return dockerCommand;
    }

    private String resolveImage(List<String> command) {
        String executable = command.get(0);

        return switch (executable) {
            case "mvn", "./mvnw" -> "maven:3.9.9-eclipse-temurin-17";
            case "gradle", "./gradlew" -> "gradle:8.10.2-jdk17";
            case "npm", "pnpm", "yarn" -> "node:22";
            case "pytest", "python", "python3" -> "python:3.11";
            case "go" -> "golang:1.24";
            case "cargo" -> "rust:1.86";
            default -> throw new IllegalArgumentException("No docker image mapping for executable: " + executable);
        };
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
