package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.ConversationWorkspaceService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultConversationWorkspaceService implements ConversationWorkspaceService {

    private final WorkspaceManager workspaceManager;

    public DefaultConversationWorkspaceService(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public void addTextFile(Conversation conversation, String relativePath, String content) {
        workspaceManager.writeTextFile(conversation, relativePath, content);
    }

    @Override
    public List<WorkspaceFileEntry> listFiles(Conversation conversation) {
        return workspaceManager.listFiles(conversation);
    }
}
