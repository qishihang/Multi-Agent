package com.qsh.multiagent.orchestration.planner;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.report.model.AggregatedResult;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import org.springframework.stereotype.Component;

@Component
public class MockPlanner implements Planner{
    @Override
    public Plan createPlan(Task task) {
        Plan plan = new Plan();
        plan.setId("Plan-" + task.getId() + "-" + task.getCurrentRound());
        plan.setTaskId(task.getId());
        plan.setRound(task.getCurrentRound());
        plan.setObjective("Mock plan objective for task: "  + task.getGoal());
        plan.setDoneCriteria("All review and test checks should pass.");

        plan.addStep(new PlanStep(
                1,
                "Mock Plan Step 1",
                "Mock plan step 1 description.",
                true
        ));
        return plan;
    }

    @Override
    public TaskDecision decide(Task task, AggregatedResult aggregatedResult){
        if(aggregatedResult.isAllPassed()){
            return TaskDecision.FINISH;
        }

        if(!task.canContinue()){
            return TaskDecision.TERMINATE;
        }

        return TaskDecision.CONTINUE;
    }
}
