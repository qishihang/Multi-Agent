package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.WorkspaceApplicationService;
import com.qsh.multiagent.infrastructure.conversation.ConversationRegistry;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;
import com.qsh.multiagent.infrastructure.workspace.resolver.WorkspaceResolver;
import com.qsh.multiagent.infrastructure.workspace.service.WorkspaceFileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultWorkspaceApplicationService implements WorkspaceApplicationService {

    private final ConversationRegistry conversationRegistry;
    private final WorkspaceResolver workspaceResolver;
    private final WorkspaceFileService workspaceFileService;

    public DefaultWorkspaceApplicationService(ConversationRegistry conversationRegistry,
                                              WorkspaceResolver workspaceResolver,
                                              WorkspaceFileService workspaceFileService) {
        this.conversationRegistry = conversationRegistry;
        this.workspaceResolver = workspaceResolver;
        this.workspaceFileService = workspaceFileService;
    }

    @Override
    public void addTextFile(String conversationId, String relativePath, String content) {
        conversationRegistry.getConversationOrThrow(conversationId);
        workspaceFileService.writeTextFile(workspaceResolver.getWorkspaceRoot(conversationId), relativePath, content);
    }

    @Override
    public List<WorkspaceFileEntry> listFiles(String conversationId) {
        conversationRegistry.getConversationOrThrow(conversationId);
        return workspaceFileService.listFiles(workspaceResolver.getWorkspaceRoot(conversationId));
    }
}
