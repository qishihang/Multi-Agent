package com.qsh.multiagent.domain.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    private String id;
    private ConversationStatus status;
    private String workspacePath;
}
