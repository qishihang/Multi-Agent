package com.qsh.multiagent.infrastructure.workspace.resolver;

import com.qsh.multiagent.domain.project.Project;
import com.qsh.multiagent.infrastructure.conversation.ConversationRegistry;
import com.qsh.multiagent.infrastructure.project.ProjectRegistry;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class WorkspaceResolver {

    private static final String BASE_WORKSPACE_DIR = "runtime-workspaces";

    private final ProjectRegistry projectRegistry;
    private final ConversationRegistry conversationRegistry;

    public WorkspaceResolver(ProjectRegistry projectRegistry,
                             ConversationRegistry conversationRegistry) {
        this.projectRegistry = projectRegistry;
        this.conversationRegistry = conversationRegistry;
    }

    public WorkspaceContext createWorkspaceForProject(Project project) {
        try {
            Path baseDir = Paths.get(BASE_WORKSPACE_DIR);
            Files.createDirectories(baseDir);

            Path projectDir = baseDir.resolve(project.getId());
            Files.createDirectories(projectDir);

            String rootPath = projectDir.toAbsolutePath().toString();
            project.setWorkspacePath(rootPath);
            projectRegistry.register(project);

            return new WorkspaceContext(project.getId(), rootPath, true);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create workspace for project: " + project.getId(), e);
        }
    }

    public WorkspaceContext buildContext(Project project) {
        String workspacePath = project.getWorkspacePath();
        if (workspacePath == null || workspacePath.isBlank()) {
            return new WorkspaceContext(project.getId(), null, false);
        }

        Path path = Paths.get(workspacePath);
        boolean available = Files.exists(path) && Files.isDirectory(path);
        return new WorkspaceContext(project.getId(), path.toAbsolutePath().toString(), available);
    }

    public Path getWorkspaceRoot(String conversationId) {
        return getWorkspaceRoot(projectRegistry.getProjectOrThrow(
                conversationRegistry.getConversationOrThrow(conversationId).getProjectId()
        ));
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
