package com.qsh.multiagent.infrastructure.sandbox.policy;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.sandbox.model.SandboxContext;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class SandboxPolicy {

    public SandboxContext buildContext(Conversation conversation, Path workspaceRoot) {
        String root = workspaceRoot.toAbsolutePath().normalize().toString();

        return new SandboxContext(
                conversation.getId(),
                root,
                List.of(root),
                List.of(
                        "mvn",
                        "./mvnw",
                        "gradle",
                        "./gradlew",
                        "npm",
                        "pnpm",
                        "yarn",
                        "pytest",
                        "python",
                        "python3",
                        "go",
                        "cargo"
                )
        );
    }

    public void validateWritePath(SandboxContext context, Path targetPath) {
        String normalizedTarget = normalize(targetPath);
        boolean allowed = context.getAllowedWriteRoots().stream()
                .anyMatch(normalizedTarget::startsWith);

        if (!allowed) {
            throw new IllegalArgumentException("Write path is outside sandbox: " + normalizedTarget);
        }
    }

    public void validateAccessPath(SandboxContext context, Path targetPath) {
        String normalizedTarget = normalize(targetPath);
        boolean allowed = context.getAllowedWriteRoots().stream()
                .anyMatch(normalizedTarget::startsWith);

        if (!allowed) {
            throw new IllegalArgumentException("Access path is outside sandbox: " + normalizedTarget);
        }
    }

    public void validateCommand(SandboxContext context, List<String> commandParts) {
        if (commandParts == null || commandParts.isEmpty()) {
            throw new IllegalArgumentException("Command must not be empty");
        }

        String executable = commandParts.get(0);
        if (!context.getAllowedCommands().contains(executable)) {
            throw new IllegalArgumentException("Executable is not allowed in sandbox: " + executable);
        }
    }

    private String normalize(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }
}
