package com.qsh.multiagent.infrastructure.workspace.manager;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.project.Project;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
public class WorkspaceManager {

    private static final String BASE_WORKSPACE_DIR = "runtime-workspaces";
    private final Map<String, Project> projects = new ConcurrentHashMap<>();
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    public WorkspaceContext createWorkspaceForProject(Project project) {
        try {
            Path baseDir = Paths.get(BASE_WORKSPACE_DIR);
            Files.createDirectories(baseDir);

            Path projectDir = baseDir.resolve(project.getId());
            Files.createDirectories(projectDir);

            String rootPath = projectDir.toAbsolutePath().toString();
            project.setWorkspacePath(rootPath);
            projects.put(project.getId(), project);

            return new WorkspaceContext(
                    project.getId(),
                    rootPath,
                    true
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create workspace for project: " + project.getId(), e);
        }
    }

    public void registerConversation(Conversation conversation) {
        if (conversation == null || conversation.getId() == null || conversation.getId().isBlank()) {
            throw new IllegalArgumentException("Conversation id must not be blank");
        }
        if (conversation.getProjectId() == null || conversation.getProjectId().isBlank()) {
            throw new IllegalArgumentException("Conversation projectId must not be blank");
        }
        getProjectOrThrow(conversation.getProjectId());
        conversations.put(conversation.getId(), conversation);
    }

    public WorkspaceContext buildContext(Project project) {
        String workspacePath = project.getWorkspacePath();

        if (workspacePath == null || workspacePath.isBlank()) {
            return new WorkspaceContext(project.getId(), null, false);
        }

        Path path = Paths.get(workspacePath);
        boolean available = Files.exists(path) && Files.isDirectory(path);

        return new WorkspaceContext(
                project.getId(),
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

    public Conversation getConversationOrThrow(String conversationId) {
        Conversation conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }
        return conversation;
    }

    public Project getProjectOrThrow(String projectId) {
        Project project = projects.get(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        return project;
    }

    public Project getProjectForConversationOrThrow(String conversationId) {
        Conversation conversation = getConversationOrThrow(conversationId);
        return getProjectOrThrow(conversation.getProjectId());
    }

    public Conversation getConversationByMemoryIdOrThrow(String memoryId) {
        return getConversationOrThrow(extractConversationId(memoryId));
    }

    public Project getProjectByMemoryIdOrThrow(String memoryId) {
        return getProjectForConversationOrThrow(extractConversationId(memoryId));
    }

    public String extractConversationId(String memoryId) {
        if (memoryId == null || memoryId.isBlank()) {
            throw new IllegalArgumentException("MemoryId must not be blank");
        }

        int separatorIndex = memoryId.indexOf("::");
        if (separatorIndex < 0) {
            return memoryId;
        }

        return memoryId.substring(0, separatorIndex);
    }

    public Path getWorkspaceRoot(Conversation conversation) {
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation must not be null");
        }
        return getWorkspaceRoot(getProjectOrThrow(conversation.getProjectId()));
    }

    public Path getWorkspaceRoot(Project project) {
        String workspacePath = project.getWorkspacePath();
        if (workspacePath == null || workspacePath.isBlank()) {
            throw new IllegalStateException("Project workspacePath is empty");
        }

        Path root = Paths.get(workspacePath).toAbsolutePath().normalize();
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalStateException("Project workspace is not available: " + workspacePath);
        }

        return root;
    }

    public void ensureInsideWorkspace(Path root, Path target) {
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Target path escapes workspace boundary");
        }
    }
}
