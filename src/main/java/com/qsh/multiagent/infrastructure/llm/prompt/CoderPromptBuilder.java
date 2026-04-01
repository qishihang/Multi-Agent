package com.qsh.multiagent.infrastructure.llm.prompt;

import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.plan.PlanStep;
import org.springframework.stereotype.Component;

@Component
public class CoderPromptBuilder {

    public String buildUserPrompt(AgentTask task, PlanArtifact planArtifact, String skillContent) {
        StringBuilder stepBuilder = new StringBuilder();

        if (planArtifact.getSteps() != null) {
            for (PlanStep step : planArtifact.getSteps()) {
                stepBuilder.append("- stepNo: ").append(step.getStepNo()).append("\n")
                        .append("  title: ").append(step.getTitle()).append("\n")
                        .append("  description: ").append(step.getDescription()).append("\n")
                        .append("  codingRequired: ").append(step.isCodingRequired()).append("\n");
            }
        }

        return """
                请你根据以下技能说明、任务上下文和计划上下文，输出结构化编码结果。

                如果你需要查看工作空间中的真实文件、搜索代码或读取文件内容，你可以按需调用可用工具。
                如果你已经明确本轮应修改或新增哪些文件，你应使用工具把修改真正写入当前工作空间。
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
                你应优先围绕 steps 中 codingRequired 为 true 的步骤展开。
                当你确认需要修改或新增文件时，请使用工具把文件真正写入当前工作空间。
                你的 changedFiles 应对应实际写入或实际计划修改的文件。
                """.formatted(
                skillContent,
                task.getTaskId(),
                task.getConversationId(),
                task.getRound(),
                task.getTaskGoal(),
                planArtifact.getObjective(),
                planArtifact.getDoneCriteria(),
                stepBuilder
        );
    }
}
