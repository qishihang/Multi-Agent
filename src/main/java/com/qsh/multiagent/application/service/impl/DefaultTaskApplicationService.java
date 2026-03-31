package com.qsh.multiagent.application.service.impl;

import com.qsh.multiagent.api.request.CreateTaskRequest;
import com.qsh.multiagent.api.response.TaskResponse;
import com.qsh.multiagent.application.service.TaskApplicationService;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import com.qsh.multiagent.orchestration.workflow.WorkflowEngine;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Deprecated
@Service
public class DefaultTaskApplicationService implements TaskApplicationService {

    private final WorkflowEngine workflowEngine;
    // 构造器注入
    public DefaultTaskApplicationService(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @Override
    public TaskResponse createAndRunTask(CreateTaskRequest  request) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setGoal(request.getGoal());
        task.setStatus(TaskStatus.CREATED);
        task.setCurrentRound(null);
        task.setMaxRounds(resolveMaxRounds(request));

        Task result = workflowEngine.run(task);
        return toResponse(result);
    }

    private Integer resolveMaxRounds(CreateTaskRequest request) {
        if (request.getMaxRounds() == null || request.getMaxRounds() <= 0) {
            return 3;
        }
        return request.getMaxRounds();
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getGoal(),
                task.getStatus(),
                task.getCurrentRound(),
                task.getMaxRounds(),
                task.getCurrentPlanId(),
                task.getFinalSummary()
        );
    }
}
