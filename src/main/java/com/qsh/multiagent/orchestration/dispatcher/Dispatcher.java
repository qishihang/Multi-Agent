package com.qsh.multiagent.orchestration.dispatcher;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.task.Task;

import java.util.List;

public interface Dispatcher {
    AgentResult<?> dispatchToCoder(Task task, Plan plan);
    List<AgentResult<?>> dispatchToVerification(Task task, Plan plan, AgentResult<?> coderResult);
}
