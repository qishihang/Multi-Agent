package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.conversation.ConversationStatus;
import com.qsh.multiagent.infrastructure.conversation.ConversationRegistry;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultConversationApplicationService implements ConversationApplicationService {

    private final ProjectApplicationService projectApplicationService;
    private final ConversationRegistry conversationRegistry;

    public DefaultConversationApplicationService(ProjectApplicationService projectApplicationService,
                                                 ConversationRegistry conversationRegistry) {
        this.projectApplicationService = projectApplicationService;
        this.conversationRegistry = conversationRegistry;
    }

    @Override
    public Conversation createConversation(String projectId) {
        projectApplicationService.getProject(projectId);

        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setStatus(ConversationStatus.CREATED);
        conversation.setProjectId(projectId);
        conversationRegistry.register(conversation);
        conversation.setStatus(ConversationStatus.ACTIVE);

        return conversation;
    }

    @Override
    public Conversation getConversation(String conversationId) {
        return conversationRegistry.getConversationOrThrow(conversationId);
    }
}
