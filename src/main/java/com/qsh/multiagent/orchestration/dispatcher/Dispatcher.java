package com.qsh.multiagent.orchestration.dispatcher;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.WorkflowRun;

import java.util.List;

public interface Dispatcher {
    AgentResult dispatchToCoder(Task task, WorkflowRun workflowRun, PlanArtifact planArtifact);
    List<AgentResult> dispatchToVerification(Task task,
                                             WorkflowRun workflowRun,
                                             PlanArtifact planArtifact,
                                             AgentResult coderResult);
}
