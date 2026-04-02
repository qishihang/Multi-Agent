package com.qsh.multiagent.infrastructure.sandbox.service;

import com.qsh.multiagent.infrastructure.sandbox.model.SandboxContext;
import com.qsh.multiagent.infrastructure.sandbox.model.ProjectRuntimeType;
import com.qsh.multiagent.infrastructure.sandbox.model.SandboxSession;
import com.qsh.multiagent.infrastructure.sandbox.model.SandboxSessionStatus;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ConversationSandboxManager {

    private static final long SANDBOX_COMMAND_TIMEOUT_SECONDS = 30;
    private static final Duration SESSION_IDLE_TIMEOUT = Duration.ofMinutes(30);
    private static final String CONTAINER_WORKSPACE = "/workspace";

    private final Map<String, SandboxSession> sessions = new ConcurrentHashMap<>();
    private final SandboxImageResolver sandboxImageResolver;
    private final ProjectRuntimeResolver projectRuntimeResolver;

    public ConversationSandboxManager(SandboxImageResolver sandboxImageResolver,
                                      ProjectRuntimeResolver projectRuntimeResolver) {
        this.sandboxImageResolver = sandboxImageResolver;
        this.projectRuntimeResolver = projectRuntimeResolver;
    }

    public SandboxSession getOrCreateSession(SandboxContext sandboxContext) {
        cleanupExpiredSessions();

        SandboxSession existing = sessions.get(sandboxContext.getConversationId());
        if (existing != null && isSessionRunning(existing)) {
            existing.setLastUsedAt(Instant.now());
            existing.setStatus(SandboxSessionStatus.ACTIVE);
            return existing;
        }

        if (existing != null) {
            releaseConversationSession(existing.getConversationId());
        }

        SandboxSession created = createSession(sandboxContext);
        sessions.put(created.getConversationId(), created);
        return created;
    }

    public void cleanupExpiredSessions() {
        Instant now = Instant.now();
        List<String> expiredConversationIds = new ArrayList<>();

        for (Map.Entry<String, SandboxSession> entry : sessions.entrySet()) {
            SandboxSession session = entry.getValue();
            Instant lastUsedAt = session.getLastUsedAt() == null ? session.getCreatedAt() : session.getLastUsedAt();
            if (lastUsedAt == null) {
                expiredConversationIds.add(entry.getKey());
                continue;
            }
            if (Duration.between(lastUsedAt, now).compareTo(SESSION_IDLE_TIMEOUT) > 0) {
                expiredConversationIds.add(entry.getKey());
            }
        }

        for (String conversationId : expiredConversationIds) {
            releaseConversationSession(conversationId);
        }
    }

    public void releaseConversationSession(String conversationId) {
        SandboxSession session = sessions.remove(conversationId);
        if (session == null) {
            return;
        }

        runCommand(List.of("docker", "rm", "-f", session.getContainerName()));
        session.setStatus(SandboxSessionStatus.CLOSED);
        session.setLastUsedAt(Instant.now());
    }

    @PreDestroy
    public void releaseAllSessions() {
        List<String> conversationIds = new ArrayList<>(sessions.keySet());
        for (String conversationId : conversationIds) {
            releaseConversationSession(conversationId);
        }
    }

    private SandboxSession createSession(SandboxContext sandboxContext) {
        ensureDockerAvailable();
        Path workspaceRoot = Path.of(sandboxContext.getWorkspaceRoot()).toAbsolutePath().normalize();
        ProjectRuntimeType runtimeType = projectRuntimeResolver.resolveRuntimeType(workspaceRoot);
        String image = sandboxImageResolver.resolveImage(runtimeType);
        String sessionId = UUID.randomUUID().toString();
        String containerName = buildContainerName(sandboxContext.getConversationId(), sessionId);

        CommandOutcome outcome = runCommand(List.of(
                "docker",
                "run",
                "-d",
                "--rm",
                "--name",
                containerName,
                "-v",
                workspaceRoot + ":" + CONTAINER_WORKSPACE,
                "-w",
                CONTAINER_WORKSPACE,
                image,
                "tail",
                "-f",
                "/dev/null"
        ));

        if (!outcome.success()) {
            throw new IllegalStateException("Failed to create sandbox session: " + outcome.stderr());
        }

        Instant now = Instant.now();
        return new SandboxSession(
                sessionId,
                sandboxContext.getConversationId(),
                sandboxContext.getProjectId(),
                workspaceRoot.toString(),
                containerName,
                image,
                SandboxSessionStatus.ACTIVE,
                now,
                now
        );
    }

    private boolean isSessionRunning(SandboxSession session) {
        CommandOutcome outcome = runCommand(List.of(
                "docker",
                "inspect",
                "-f",
                "{{.State.Running}}",
                session.getContainerName()
        ));
        return outcome.success() && "true".equalsIgnoreCase(outcome.stdout().trim());
    }

    private void ensureDockerAvailable() {
        CommandOutcome outcome = runCommand(List.of("docker", "version", "--format", "{{.Server.Version}}"));
        if (!outcome.success()) {
            throw new IllegalStateException("Docker is not available for sandbox execution: " + outcome.stderr());
        }
    }

    private String buildContainerName(String conversationId, String sessionId) {
        String normalizedConversationId = conversationId.replaceAll("[^a-zA-Z0-9_.-]", "-");
        return "multi-agent-" + normalizedConversationId + "-" + sessionId.substring(0, 8);
    }

    private CommandOutcome runCommand(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            boolean finished = process.waitFor(SANDBOX_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CommandOutcome(false, "", "Command timed out: " + String.join(" ", command));
            }

            String stdout;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                stdout = reader.lines().collect(Collectors.joining("\n"));
            }

            String stderr;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                stderr = reader.lines().collect(Collectors.joining("\n"));
            }

            return new CommandOutcome(process.exitValue() == 0, stdout, stderr);
        } catch (IOException e) {
            return new CommandOutcome(false, "", "Failed to execute docker command: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CommandOutcome(false, "", "Docker command execution interrupted");
        }
    }

    private record CommandOutcome(boolean success, String stdout, String stderr) {
    }
}
