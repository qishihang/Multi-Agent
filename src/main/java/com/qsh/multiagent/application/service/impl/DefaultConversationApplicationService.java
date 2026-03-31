package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.conversation.ConversationStatus;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultConversationApplicationService implements ConversationApplicationService {

    private final WorkspaceManager workspaceManager;

    public DefaultConversationApplicationService(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Conversation createConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setStatus(ConversationStatus.CREATED);

        workspaceManager.createWorkspaceForConversation(conversation);
        conversation.setStatus(ConversationStatus.ACTIVE);

        return conversation;
    }
}
