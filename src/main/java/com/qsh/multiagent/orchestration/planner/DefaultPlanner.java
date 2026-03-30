package com.qsh.multiagent.orchestration.planner;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.report.model.AggregatedResult;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import com.qsh.multiagent.infrastructure.llm.service.PlannerAiService;
import com.qsh.multiagent.infrastructure.llm.service.PlannerPlanOutput;
import com.qsh.multiagent.infrastructure.llm.service.PlannerPlanStepOutput;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DefaultPlanner implements Planner{

    private final PlannerAiService plannerAiService;

    public DefaultPlanner(PlannerAiService plannerAiService) {
        this.plannerAiService = plannerAiService;
    }

    @Override
    public Plan createPlan(Task task) {
        String prompt = buildPlanningPrompt(task);
        PlannerPlanOutput output = plannerAiService.createPlan(task.getId(), prompt);

        Plan plan = new Plan();
        plan.setId("plan-" + task.getId() + "-" + task.getCurrentRound());
        plan.setTaskId(task.getId());
        plan.setRound(task.getCurrentRound());
        plan.setObjective(output.objective());
        plan.setDoneCriteria(output.doneCriteria());

        if (output.steps() != null) {
            for (PlannerPlanStepOutput stepOutput : output.steps()) {
                PlanStep step = new PlanStep();
                step.setStepNo(stepOutput.stepNo());
                step.setTitle(stepOutput.title());
                step.setDescription(stepOutput.description());
                step.setCodingRequired(Boolean.TRUE.equals(stepOutput.codingRequired()));
                plan.addStep(step);
            }
        }

        return plan;
    }

    @Override
    public TaskDecision decide(Task task, AggregatedResult aggregatedResult) {
        if (aggregatedResult.isAllPassed()) {
            return TaskDecision.FINISH;
        }

        if (!task.canContinue()) {
            return TaskDecision.TERMINATE;
        }

        return TaskDecision.CONTINUE;
    }

    private String buildPlanningPrompt(Task task) {
        return """
                Task ID: %s
                Current Round: %s
                Task Goal: %s

                Create a short execution plan for this round.
                """.formatted(task.getId(), task.getCurrentRound(), task.getGoal());
    }
}
