package com.qsh.multiagent.infrastructure.llm.prompt;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.task.Task;
import org.springframework.stereotype.Component;

@Component
public class ReviewerPromptBuilder {

    public String buildUserPrompt(Task task, Plan plan, String skillContent){

        StringBuilder stepBuilder = new StringBuilder();
        if(plan.getSteps() != null){
            for(PlanStep step: plan.getSteps()){
                stepBuilder.append("- stepNo: ").append(step.getStepNo()).append("\n")
                        .append("  title: ").append(step.getTitle()).append("\n")
                        .append("  description: ").append(step.getDescription()).append("\n")
                        .append("  codingRequired: ").append(step.isCodingRequired()).append("\n");
            }
        }

        return """
                请你根据以下技能说明和当前轮次计划上下文，输出结构化审查结果。

                ====================
                【Skill】
                ====================
                %s

                ====================
                【Task Context】
                ====================
                taskId: %s
                currentRound: %s
                taskGoal: %s

                ====================
                【Plan Context】
                ====================
                currentPlanObjective: %s
                currentPlanDoneCriteria: %s
                currentPlanSteps:
                %s

                ====================
                【Your Task】
                ====================
                请审查当前轮次计划是否合理、清晰、可执行、可验证，
                并输出结构化审查结果。
                """.formatted(
                skillContent,
                task.getId(),
                task.getCurrentRound(),
                task.getGoal(),
                plan.getObjective(),
                plan.getDoneCriteria(),
                stepBuilder
        );
    }
}
