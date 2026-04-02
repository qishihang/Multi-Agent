package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.WorkspaceApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultWorkspaceApplicationService implements WorkspaceApplicationService {

    private final WorkspaceManager workspaceManager;

    public DefaultWorkspaceApplicationService(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public void addTextFile(String conversationId, String relativePath, String content) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        workspaceManager.writeTextFile(conversation, relativePath, content);
    }

    @Override
    public List<WorkspaceFileEntry> listFiles(String conversationId) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        return workspaceManager.listFiles(conversation);
    }
}
