package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.conversation.ConversationStatus;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultConversationApplicationService implements ConversationApplicationService {

    private final ProjectApplicationService projectApplicationService;
    private final WorkspaceManager workspaceManager;

    public DefaultConversationApplicationService(ProjectApplicationService projectApplicationService,
                                                 WorkspaceManager workspaceManager) {
        this.projectApplicationService = projectApplicationService;
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Conversation createConversation(String projectId) {
        projectApplicationService.getProject(projectId);

        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setStatus(ConversationStatus.CREATED);
        conversation.setProjectId(projectId);
        workspaceManager.registerConversation(conversation);
        conversation.setStatus(ConversationStatus.ACTIVE);

        return conversation;
    }

    @Override
    public Conversation getConversation(String conversationId) {
        return workspaceManager.getConversationOrThrow(conversationId);
    }
}
