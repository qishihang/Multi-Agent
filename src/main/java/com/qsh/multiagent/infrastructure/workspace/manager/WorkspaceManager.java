package com.qsh.multiagent.infrastructure.workspace.manager;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceContext;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class WorkspaceManager {

    private static final String BASE_WORKSPACE_DIR = "runtime-workspaces";

    public WorkspaceContext createWorkspaceForConversation(Conversation conversation) {
        try {
            Path baseDir = Paths.get(BASE_WORKSPACE_DIR);
            Files.createDirectories(baseDir);

            Path conversationDir = baseDir.resolve(conversation.getId());
            Files.createDirectories(conversationDir);

            String rootPath = conversationDir.toAbsolutePath().toString();
            conversation.setWorkspacePath(rootPath);

            return new WorkspaceContext(
                    conversation.getId(),
                    rootPath,
                    true
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create workspace for conversation: " + conversation.getId(), e);
        }
    }

    public WorkspaceContext buildContext(Conversation conversation) {
        String workspacePath = conversation.getWorkspacePath();

        if (workspacePath == null || workspacePath.isBlank()) {
            return new WorkspaceContext(conversation.getId(), null, false);
        }

        Path path = Paths.get(workspacePath);
        boolean available = Files.exists(path) && Files.isDirectory(path);

        return new WorkspaceContext(
                conversation.getId(),
                path.toAbsolutePath().toString(),
                available
        );
    }

    public void writeTextFile(Conversation conversation, String relativePath, String content) {
        try {
            Path root = getWorkspaceRoot(conversation);
            Path target = root.resolve(relativePath).normalize();

            ensureInsideWorkspace(root, target);

            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    target,
                    content == null ? "" : content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write file into workspace: " + relativePath, e);
        }
    }

    public List<WorkspaceFileEntry> listFiles(Conversation conversation) {
        try {
            Path root = getWorkspaceRoot(conversation);
            List<WorkspaceFileEntry> result = new ArrayList<>();

            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(path -> !path.equals(root))
                        .forEach(path -> {
                            String relative = root.relativize(path).toString();
                            result.add(new WorkspaceFileEntry(
                                    relative,
                                    Files.isDirectory(path)
                            ));
                        });
            }

            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list workspace files for conversation: " + conversation.getId(), e);
        }
    }

    private Path getWorkspaceRoot(Conversation conversation) {
        String workspacePath = conversation.getWorkspacePath();
        if (workspacePath == null || workspacePath.isBlank()) {
            throw new IllegalStateException("Conversation workspacePath is empty");
        }

        Path root = Paths.get(workspacePath).toAbsolutePath().normalize();
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalStateException("Conversation workspace is not available: " + workspacePath);
        }

        return root;
    }

    private void ensureInsideWorkspace(Path root, Path target) {
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Target path escapes workspace boundary");
        }
    }
}
