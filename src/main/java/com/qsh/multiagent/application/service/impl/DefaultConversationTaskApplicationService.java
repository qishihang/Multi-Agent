package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.application.service.ConversationTaskApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import com.qsh.multiagent.orchestration.workflow.WorkflowEngine;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultConversationTaskApplicationService implements ConversationTaskApplicationService {

    private final WorkflowEngine workflowEngine;

    public DefaultConversationTaskApplicationService(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @Override
    public Task createAndRunTask(Conversation conversation, String goal, Integer maxRounds) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setConversationId(conversation.getId());
        task.setGoal(goal);
        task.setStatus(TaskStatus.CREATED);
        task.setCurrentRound(null);
        task.setMaxRounds(resolveMaxRounds(maxRounds));

        return workflowEngine.run(task);
    }

    private Integer resolveMaxRounds(Integer maxRounds) {
        if (maxRounds == null || maxRounds <= 0) {
            return 3;
        }
        return maxRounds;
    }
}
