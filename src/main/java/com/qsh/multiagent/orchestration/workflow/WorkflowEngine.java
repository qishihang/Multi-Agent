package com.qsh.multiagent.orchestration.workflow;

import com.qsh.multiagent.domain.task.Task;

public interface WorkflowEngine {
    Task run(Task task);
}
