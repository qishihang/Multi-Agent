package com.qsh.multiagent.conversation;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.conversation.ConversationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConversationApplicationServiceTest {

    @Autowired
    private ConversationApplicationService conversationApplicationService;

    @Test
    void should_create_conversation_with_workspace() {
        Conversation conversation = conversationApplicationService.createConversation();

        Assertions.assertNotNull(conversation);
        Assertions.assertNotNull(conversation.getId());
        Assertions.assertEquals(ConversationStatus.ACTIVE, conversation.getStatus());
        Assertions.assertNotNull(conversation.getWorkspacePath());
    }
}
