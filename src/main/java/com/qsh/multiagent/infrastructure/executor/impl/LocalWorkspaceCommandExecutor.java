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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class LocalWorkspaceCommandExecutor implements WorkspaceCommandExecutor {

    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    private final SandboxPolicy sandboxPolicy;

    public LocalWorkspaceCommandExecutor(SandboxPolicy sandboxPolicy) {
        this.sandboxPolicy = sandboxPolicy;
    }

    @Override
    public CommandExecutionResult execute(SandboxContext sandboxContext, List<String> command) {
        sandboxPolicy.validateCommand(sandboxContext, command);

        Path root = Path.of(sandboxContext.getWorkspaceRoot()).toAbsolutePath().normalize();
        sandboxPolicy.validateAccessPath(sandboxContext, root);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(root.toFile());

        try {
            Process process = processBuilder.start();

            boolean finished = process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CommandExecutionResult(
                        false,
                        -1,
                        "",
                        "Command timed out after " + DEFAULT_TIMEOUT_SECONDS + " seconds"
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
            throw new IllegalStateException("Failed to execute command in sandbox: " + command, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Command execution interrupted: " + command, e);
        }
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
