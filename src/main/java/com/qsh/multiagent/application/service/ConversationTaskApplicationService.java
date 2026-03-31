package com.qsh.multiagent.application.service;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.task.Task;

public interface ConversationTaskApplicationService {

    Task createAndRunTask(Conversation conversation, String goal, Integer maxRounds);
}
