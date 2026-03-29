package com.qsh.multiagent.orchestration.planner;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.report.model.AggregatedResult;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.TaskDecision;

public interface Planner {
    Plan createPlan(Task task);
    TaskDecision decide(Task task, AggregatedResult aggregatedResult);
}
