package com.qsh.multiagent.api.controller;

import com.qsh.multiagent.api.response.ConversationResponse;
import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/conversations")
public class ConversationController {

    private final ConversationApplicationService conversationApplicationService;

    public ConversationController(ConversationApplicationService conversationApplicationService) {
        this.conversationApplicationService = conversationApplicationService;
    }

    @PostMapping
    public ConversationResponse createConversation(@PathVariable String projectId) {
        Conversation conversation = conversationApplicationService.createConversation(projectId);
        return new ConversationResponse(
                conversation.getId(),
                conversation.getProjectId(),
                conversation.getStatus()
        );
    }
}
