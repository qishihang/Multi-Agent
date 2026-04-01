package com.qsh.multiagent.orchestration.planner;

import com.qsh.multiagent.domain.artifact.AggregateArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import com.qsh.multiagent.domain.workflow.WorkflowRun;

public interface Planner {
    PlanArtifact createPlanArtifact(Task task);

    TaskDecision decide(Task task, WorkflowRun workflowRun, AggregateArtifact aggregateArtifact);
}
