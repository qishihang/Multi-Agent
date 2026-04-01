package com.qsh.multiagent.infrastructure.llm.prompt;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.task.Task;
import org.springframework.stereotype.Component;

@Component
public class CoderPromptBuilder {

    public String buildUserPrompt(Task task, Plan plan, String skillContent) {
        StringBuilder stepBuilder = new StringBuilder();

        if (plan.getSteps() != null) {
            for (PlanStep step : plan.getSteps()) {
                stepBuilder.append("- stepNo: ").append(step.getStepNo()).append("\n")
                        .append("  title: ").append(step.getTitle()).append("\n")
                        .append("  description: ").append(step.getDescription()).append("\n")
                        .append("  codingRequired: ").append(step.isCodingRequired()).append("\n");
            }
        }

        return """
                请你根据以下技能说明、任务上下文和计划上下文，输出结构化编码结果。

                如果你需要查看工作空间中的真实文件、搜索代码或读取文件内容，你可以按需调用可用工具。
                不要臆造文件内容，缺少上下文时优先使用工具。

                ====================
                【Skill】
                ====================
                %s

                ====================
                【Task Context】
                ====================
                taskId: %s
                conversationId: %s
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
                请基于上述上下文，为当前轮输出结构化编码结果。
                当前阶段不要求你真正写文件，但必须输出清晰、具体、可执行的编码结果。
                """.formatted(
                skillContent,
                task.getId(),
                task.getConversationId(),
                task.getCurrentRound(),
                task.getGoal(),
                plan.getObjective(),
                plan.getDoneCriteria(),
                stepBuilder
        );
    }
}
