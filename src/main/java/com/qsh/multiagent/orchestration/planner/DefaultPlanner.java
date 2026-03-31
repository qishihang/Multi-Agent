package com.qsh.multiagent.orchestration.planner;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.report.model.AggregatedResult;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import com.qsh.multiagent.infrastructure.llm.prompt.PlannerPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.PlannerAiService;
import com.qsh.multiagent.infrastructure.llm.service.PlannerPlanOutput;
import com.qsh.multiagent.infrastructure.llm.service.PlannerPlanStepOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DefaultPlanner implements Planner{

    private static final String PLANNER_SKILL_PATH = "skills/planner-skill.md";

    private final PlannerAiService plannerAiService;
    private final PlannerPromptBuilder plannerPromptBuilder;
    private final SkillLoader skillLoader;


    public DefaultPlanner(PlannerAiService plannerAiService,
                          PlannerPromptBuilder plannerPromptBuilder,
                          SkillLoader skillLoader) {
        this.plannerAiService = plannerAiService;
        this.plannerPromptBuilder = plannerPromptBuilder;
        this.skillLoader = skillLoader;
    }

    @Override
    public Plan createPlan(Task task) {
        String skillContent = skillLoader.loadSkill(PLANNER_SKILL_PATH);
        String prompt = plannerPromptBuilder.buildUserPrompt(task, skillContent);
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
}
