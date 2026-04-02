package com.qsh.multiagent.application.service;

import com.qsh.multiagent.domain.conversation.Conversation;

public interface ConversationApplicationService {

    Conversation createConversation(String projectId);

    Conversation getConversation(String conversationId);
}
