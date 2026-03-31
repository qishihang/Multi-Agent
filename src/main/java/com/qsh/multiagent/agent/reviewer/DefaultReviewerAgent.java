package com.qsh.multiagent.agent.reviewer;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.report.model.ReviewReport;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.infrastructure.llm.prompt.ReviewerPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.ReviewerAiService;
import com.qsh.multiagent.infrastructure.llm.service.ReviewerAnalysisOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DefaultReviewerAgent implements Agent {

    private static final String REVIEWER_SKILL_PATH = "skills/reviewer-skill.md";

    private final ReviewerAiService reviewerAiService;
    private final ReviewerPromptBuilder reviewerPromptBuilder;
    private final SkillLoader skillLoader;

    public DefaultReviewerAgent(ReviewerAiService reviewerAiService,
                                ReviewerPromptBuilder reviewerPromptBuilder,
                                SkillLoader skillLoader) {
        this.reviewerAiService = reviewerAiService;
        this.reviewerPromptBuilder = reviewerPromptBuilder;
        this.skillLoader = skillLoader;
    }

    @Override
    public AgentType getType() {
        return AgentType.REVIEWER;
    }

    @Override
    public AgentResult<ReviewReport> execute(AgentTask task) {
        Task taskContext = task.getTask();
        Plan planContext = task.getPlan();

        String skillContent = skillLoader.loadSkill(REVIEWER_SKILL_PATH);
        String prompt = reviewerPromptBuilder.buildUserPrompt(taskContext, planContext, skillContent);
        ReviewerAnalysisOutput output = reviewerAiService.review(prompt);

        // 利用模型输出对象构造领域对象
        ReviewReport report = new ReviewReport(
                Boolean.TRUE.equals(output.passed()),
                output.issueCount(),
                output.blockingIssueCount(),
                output.details()
        );

        // 返回统一的AgentReport
        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                report.isPassed(),
                report.getDetails(),
                report,
                null
        );
    }
}
