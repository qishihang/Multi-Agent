package com.qsh.multiagent.api.response;

import com.qsh.multiagent.domain.conversation.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {

    private String conversationId;
    private String projectId;
    private ConversationStatus status;
}
