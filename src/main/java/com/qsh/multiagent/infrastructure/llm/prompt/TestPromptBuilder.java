package com.qsh.multiagent.infrastructure.llm.prompt;

import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.domain.artifact.CodeArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.plan.PlanStep;
import org.springframework.stereotype.Component;

@Component
public class TestPromptBuilder {

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
                请你根据以下技能说明、任务上下文、计划上下文和编码结果上下文，输出结构化测试报告。

                如果需要查看工作空间文件、搜索代码、读取文件、写入测试文件或执行验证命令，你可以按需调用工具。
                你需要自己判断项目类型、是否需要先准备依赖、是否需要准备构建环境、是否需要编译、是否需要生成测试，以及应该执行什么验证命令。
                对于存在依赖管理文件的项目，你应优先：
                1. 调用 detectProjectType 获取项目类型
                2. 调用 prepareDependencies 准备依赖
                3. 如有必要再调用 prepareBuildEnvironment
                4. 最后调用 runVerificationCommand 执行验证
                如果依赖准备失败，你必须在 failureAnalysis 中明确说明，并且不要声称测试已经可靠通过。

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
                你需要从测试智能体的角度，判断如何验证当前轮编码结果。
                你可以生成测试、写入测试文件、准备依赖、准备构建环境、执行验证命令，并最终输出总结后的结构化测试报告。
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
