package com.qsh.multiagent.conversation;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.conversation.ConversationStatus;
import com.qsh.multiagent.domain.project.Project;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConversationApplicationServiceTest {

    @Autowired
    private ConversationApplicationService conversationApplicationService;

    @Autowired
    private ProjectApplicationService projectApplicationService;

    @Test
    void should_create_conversation_with_project_reference() {
        Project project = projectApplicationService.createProject();
        Conversation conversation = conversationApplicationService.createConversation(project.getId());

        Assertions.assertNotNull(conversation);
        Assertions.assertNotNull(conversation.getId());
        Assertions.assertEquals(ConversationStatus.ACTIVE, conversation.getStatus());
        Assertions.assertNotNull(conversation.getProjectId());
        Assertions.assertNotNull(project);
        Assertions.assertEquals(project.getId(), conversation.getProjectId());
        Assertions.assertNotNull(project.getWorkspacePath());
    }

    @Test
    void should_create_multiple_conversations_for_same_project() {
        Project project = projectApplicationService.createProject();

        Conversation firstConversation = conversationApplicationService.createConversation(project.getId());
        Conversation secondConversation = conversationApplicationService.createConversation(project.getId());

        Assertions.assertNotNull(firstConversation);
        Assertions.assertNotNull(secondConversation);
        Assertions.assertNotEquals(firstConversation.getId(), secondConversation.getId());
        Assertions.assertEquals(project.getId(), firstConversation.getProjectId());
        Assertions.assertEquals(project.getId(), secondConversation.getProjectId());
    }
}
