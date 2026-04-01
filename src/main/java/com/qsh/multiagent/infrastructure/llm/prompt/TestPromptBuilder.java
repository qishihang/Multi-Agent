package com.qsh.multiagent.infrastructure.llm.prompt;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.report.model.CoderReport;
import com.qsh.multiagent.domain.task.Task;
import org.springframework.stereotype.Component;

@Component
public class TestPromptBuilder {

    public String buildUserPrompt(Task task, Plan plan, CoderReport coderReport, String skillContent) {
        StringBuilder stepBuilder = new StringBuilder();
        if (plan.getSteps() != null) {
            for (PlanStep step : plan.getSteps()) {
                stepBuilder.append("- stepNo: ").append(step.getStepNo()).append("\n")
                        .append("  title: ").append(step.getTitle()).append("\n")
                        .append("  description: ").append(step.getDescription()).append("\n")
                        .append("  codingRequired: ").append(step.isCodingRequired()).append("\n");
            }
        }

        String coderSummary = coderReport != null ? safe(coderReport.getChangeSummary()) : "当前轮暂无编码结果摘要。";
        String changedFiles = coderReport != null && coderReport.getChangedFiles() != null
                ? String.join("\n", coderReport.getChangedFiles())
                : "当前轮暂无文件变更信息。";
        String codeDraft = coderReport != null ? safe(coderReport.getCodeDraft()) : "当前轮暂无代码草案。";
        String coderRisks = coderReport != null ? safe(coderReport.getRisks()) : "当前轮暂无风险说明。";

        return """
                请你根据以下技能说明、任务上下文、计划上下文和编码结果上下文，输出结构化测试报告。

                如果需要查看工作空间文件、搜索代码、读取文件、写入测试文件或执行验证命令，你可以按需调用工具。
                你需要自己判断项目类型、是否需要编译、是否需要生成测试，以及应该执行什么验证命令。

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
                你可以生成测试、写入测试文件、执行验证命令，并最终输出总结后的结构化测试报告。
                """.formatted(
                skillContent,
                task.getId(),
                task.getConversationId(),
                task.getCurrentRound(),
                task.getGoal(),
                plan.getObjective(),
                plan.getDoneCriteria(),
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
