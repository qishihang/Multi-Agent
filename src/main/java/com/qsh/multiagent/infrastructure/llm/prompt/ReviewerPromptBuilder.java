package com.qsh.multiagent.infrastructure.llm.prompt;

import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.domain.artifact.CodeArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.plan.PlanStep;
import org.springframework.stereotype.Component;

@Component
public class ReviewerPromptBuilder {

    public String buildUserPrompt(AgentTask task, PlanArtifact planArtifact, CodeArtifact codeArtifact, String skillContent) {

        StringBuilder stepBuilder = new StringBuilder();
        if (planArtifact.getSteps() != null) {
            for (PlanStep step : planArtifact.getSteps()) {
                stepBuilder.append("- stepNo: ").append(step.getStepNo()).append("\n")
                        .append("  title: ").append(step.getTitle()).append("\n")
                        .append("  description: ").append(step.getDescription()).append("\n")
                        .append("  codingRequired: ").append(step.isCodingRequired()).append("\n");
            }
        }

        String coderSummary = codeArtifact != null ? safe(codeArtifact.getChangeSummary()) : "当前轮暂无编码结果摘要。";
        String changedFiles = codeArtifact != null && codeArtifact.getChangedFiles() != null
                ? String.join("\n", codeArtifact.getChangedFiles())
                : "当前轮暂无文件变更信息。";
        String codeDraft = codeArtifact != null ? safe(codeArtifact.getCodeDraft()) : "当前轮暂无代码草案。";
        String coderRisks = codeArtifact != null ? safe(codeArtifact.getRisks()) : "当前轮暂无风险说明。";

        return """
                请你根据以下技能说明、当前轮次计划上下文和编码结果上下文，输出结构化审查结果。

                如果你需要查看工作空间中的真实文件、搜索代码或读取文件内容，你可以按需调用可用工具。
                不要臆造文件内容；当需要核对实现细节时，优先使用工具获取证据。

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
                【Coder Context】
                ====================
                changeSummary: %s
                changedFiles:
                %s
                codeDraft:
                %s
                risks:
                %s

                ====================
                【Your Task】
                ====================
                请审查当前轮次计划以及当前编码结果是否与计划一致、是否合理、是否清晰、是否可验证，
                并输出结构化审查结果。
                """.formatted(
                skillContent,
                task.getTaskId(),
                task.getConversationId(),
                task.getRound(),
                task.getTaskGoal(),
                planArtifact.getObjective(),
                planArtifact.getDoneCriteria(),
                stepBuilder,
                coderSummary,
                changedFiles,
                codeDraft,
                coderRisks
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "无" : value;
    }
}
