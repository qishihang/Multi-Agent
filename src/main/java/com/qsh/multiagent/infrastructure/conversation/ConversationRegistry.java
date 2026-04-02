package com.qsh.multiagent.infrastructure.conversation;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.project.ProjectRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationRegistry {

    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    private final ProjectRegistry projectRegistry;

    public ConversationRegistry(ProjectRegistry projectRegistry) {
        this.projectRegistry = projectRegistry;
    }

    public void register(Conversation conversation) {
        if (conversation == null || conversation.getId() == null || conversation.getId().isBlank()) {
            throw new IllegalArgumentException("Conversation id must not be blank");
        }
        if (conversation.getProjectId() == null || conversation.getProjectId().isBlank()) {
            throw new IllegalArgumentException("Conversation projectId must not be blank");
        }
        projectRegistry.getProjectOrThrow(conversation.getProjectId());
        conversations.put(conversation.getId(), conversation);
    }

    public Conversation getConversationOrThrow(String conversationId) {
        Conversation conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }
        return conversation;
    }

    public Conversation getConversationByMemoryIdOrThrow(String memoryId) {
        return getConversationOrThrow(extractConversationId(memoryId));
    }

    public String extractConversationId(String memoryId) {
        if (memoryId == null || memoryId.isBlank()) {
            throw new IllegalArgumentException("MemoryId must not be blank");
        }

        int separatorIndex = memoryId.indexOf("::");
        if (separatorIndex < 0) {
            return memoryId;
        }

        return memoryId.substring(0, separatorIndex);
    }
}
