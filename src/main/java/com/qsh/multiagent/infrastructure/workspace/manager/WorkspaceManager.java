package com.qsh.multiagent.infrastructure.workspace.manager;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}
