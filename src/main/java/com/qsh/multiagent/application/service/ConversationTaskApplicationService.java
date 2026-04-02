package com.qsh.multiagent.application.service;

import com.qsh.multiagent.domain.task.Task;

public interface ConversationTaskApplicationService {

    Task createAndRunTask(String conversationId, String goal, Integer maxRounds);
}
