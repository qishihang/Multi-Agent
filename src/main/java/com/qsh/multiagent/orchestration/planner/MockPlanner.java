package com.qsh.multiagent.orchestration.planner;

import com.qsh.multiagent.domain.artifact.AggregateArtifact;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import com.qsh.multiagent.domain.workflow.WorkflowRun;
import org.springframework.stereotype.Component;

//@Component
public class MockPlanner implements Planner{
    @Override
    public PlanArtifact createPlanArtifact(Task task) {
        PlanArtifact artifact = new PlanArtifact();
        artifact.setArtifactId("plan-" + task.getId() + "-" + task.getCurrentRound());
        artifact.setTaskId(task.getId());
        artifact.setConversationId(task.getConversationId());
        artifact.setRound(task.getCurrentRound());
        artifact.setObjective("Mock plan objective for task: "  + task.getGoal());
        artifact.setDoneCriteria("All review and test checks should pass.");

        artifact.addStep(new PlanStep(
                1,
                "Mock Plan Step 1",
                "Mock plan step 1 description.",
                true
        ));
        return artifact;
    }

    @Override
    public TaskDecision decide(Task task, WorkflowRun workflowRun, AggregateArtifact aggregateArtifact){
        if(aggregateArtifact.isAllPassed()){
            return TaskDecision.FINISH;
        }

        if(workflowRun == null || !workflowRun.canContinue(task.getMaxRounds())){
            return TaskDecision.TERMINATE;
        }

        return TaskDecision.CONTINUE;
    }
}
