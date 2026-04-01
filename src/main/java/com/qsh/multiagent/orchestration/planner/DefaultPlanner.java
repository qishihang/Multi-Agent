package com.qsh.multiagent.orchestration.planner;

import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.AggregateArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import com.qsh.multiagent.domain.workflow.WorkflowRun;
import com.qsh.multiagent.infrastructure.llm.prompt.PlannerPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.PlannerAiService;
import com.qsh.multiagent.infrastructure.llm.service.PlannerPlanOutput;
import com.qsh.multiagent.infrastructure.llm.service.PlannerPlanStepOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;

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
    public PlanArtifact createPlanArtifact(Task task) {
        String skillContent = skillLoader.loadSkill(PLANNER_SKILL_PATH);
        String prompt = plannerPromptBuilder.buildUserPrompt(task, skillContent);
        PlannerPlanOutput output = plannerAiService.createPlan(task.getConversationId(), prompt);

        PlanArtifact artifact = new PlanArtifact(
                buildPlanArtifactId(task),
                task.getConversationId(),
                task.getId(),
                null,
                task.getCurrentRound(),
                AgentType.PLANNER,
                Instant.now()
        );
        artifact.setObjective(output.objective());
        artifact.setDoneCriteria(output.doneCriteria());
        artifact.addRecommendedAgentRole(AgentType.CODER);
        artifact.addRecommendedVerifier(AgentType.REVIEWER);
        artifact.addRecommendedVerifier(AgentType.TESTER);
        artifact.setStopConditionHint("Stop when review and test results have no blocking issues.");
        artifact.setReplanStrategyHint("If verification finds issues, continue with the next round and revise the plan.");

        if (output.steps() != null) {
            for (PlannerPlanStepOutput stepOutput : output.steps()) {
                com.qsh.multiagent.domain.plan.PlanStep step = new com.qsh.multiagent.domain.plan.PlanStep();
                step.setStepNo(stepOutput.stepNo());
                step.setTitle(stepOutput.title());
                step.setDescription(stepOutput.description());
                step.setCodingRequired(Boolean.TRUE.equals(stepOutput.codingRequired()));
                artifact.addStep(step);
            }
        }

        return artifact;
    }

    @Override
    public TaskDecision decide(Task task, WorkflowRun workflowRun, AggregateArtifact aggregateArtifact) {
        if (aggregateArtifact == null) {
            return TaskDecision.TERMINATE;
        }

        if (aggregateArtifact.isAllPassed()) {
            return TaskDecision.FINISH;
        }

        if (workflowRun == null || !workflowRun.canContinue(task.getMaxRounds())) {
            return TaskDecision.TERMINATE;
        }

        if (aggregateArtifact.getRecommendedDecision() != null) {
            return aggregateArtifact.getRecommendedDecision();
        }

        return TaskDecision.CONTINUE;
    }

    private String buildPlanArtifactId(Task task) {
        return "plan-" + task.getId() + "-" + task.getCurrentRound();
    }
}
