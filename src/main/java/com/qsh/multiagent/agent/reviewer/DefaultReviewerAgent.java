package com.qsh.multiagent.agent.reviewer;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.CodeArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.artifact.ReviewArtifact;
import com.qsh.multiagent.infrastructure.llm.prompt.ReviewerPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.ReviewerAiService;
import com.qsh.multiagent.infrastructure.llm.service.ReviewerAnalysisOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;

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
    public AgentResult execute(AgentTask task) {
        PlanArtifact planArtifact = requirePlanArtifact(task);
        CodeArtifact codeArtifact = requireCodeArtifact(task);

        String skillContent = skillLoader.loadSkill(REVIEWER_SKILL_PATH);
        String prompt = reviewerPromptBuilder.buildUserPrompt(task, planArtifact, codeArtifact, skillContent);
        ReviewerAnalysisOutput output = reviewerAiService.review(
                buildExecutionMemoryId(task),
                prompt
        );

        AgentResult result = new AgentResult(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                Boolean.TRUE.equals(output.passed()),
                output.details(),
                null,
                null,
                null,
                null
        );
        ReviewArtifact reviewArtifact = buildReviewArtifact(task, output);
        result.addOutputArtifact(reviewArtifact);
        result.setRawEvidence(output.details());

        if (output.blockingIssueCount() != null && output.blockingIssueCount() > 0) {
            result.addIssue("Blocking review issues: " + output.blockingIssueCount());
        }
        if (output.issueCount() != null && output.issueCount() > 0) {
            result.addIssue("Total review issues: " + output.issueCount());
        }

        return result;
    }

    private String buildExecutionMemoryId(AgentTask task) {
        if (task.getMemoryScope() != null && !task.getMemoryScope().isBlank()) {
            return task.getMemoryScope();
        }
        return "%s::reviewer::task-%s::round-%s".formatted(
                task.getConversationId(),
                task.getTaskId(),
                task.getRound()
        );
    }

    private ReviewArtifact buildReviewArtifact(AgentTask task,
                                               ReviewerAnalysisOutput output) {
        ReviewArtifact artifact = new ReviewArtifact(
                buildReviewArtifactId(task),
                resolveConversationId(task),
                task.getTaskId(),
                task.getRunId(),
                task.getRound(),
                AgentType.REVIEWER,
                Instant.now()
        );
        artifact.setPassed(Boolean.TRUE.equals(output.passed()));
        artifact.setIssueCount(output.issueCount());
        artifact.setBlockingIssueCount(output.blockingIssueCount());
        artifact.setDetails(output.details());
        artifact.setPlanAlignmentSummary(buildPlanAlignmentSummary(output, task));
        artifact.setEvidenceSummary(buildEvidenceSummary(task, output));
        return artifact;
    }

    private String buildReviewArtifactId(AgentTask task) {
        return "review-" + task.getTaskId() + "-" + task.getRound();
    }

    private String resolveConversationId(AgentTask task) {
        return task.getConversationId();
    }

    private String buildPlanAlignmentSummary(ReviewerAnalysisOutput output, AgentTask task) {
        if (output.passed() != null && output.passed()) {
            return "Review indicates the implementation is aligned with the current plan.";
        }
        return "Review found issues that require checking plan alignment for the current round.";
    }

    private String buildEvidenceSummary(AgentTask task, ReviewerAnalysisOutput output) {
        String planId = task.getPlanId() == null ? "unknown-plan" : task.getPlanId();
        Integer issueCount = output.issueCount() == null ? 0 : output.issueCount();
        return "Review evidence based on plan %s with %s identified issues.".formatted(planId, issueCount);
    }

    private PlanArtifact requirePlanArtifact(AgentTask task) {
        PlanArtifact artifact = task.findInputArtifact(PlanArtifact.class);
        if (artifact == null) {
            throw new IllegalStateException("ReviewerAgent requires PlanArtifact input");
        }
        return artifact;
    }

    private CodeArtifact requireCodeArtifact(AgentTask task) {
        CodeArtifact artifact = task.findInputArtifact(CodeArtifact.class);
        if (artifact == null) {
            throw new IllegalStateException("ReviewerAgent requires CodeArtifact input");
        }
        return artifact;
    }
}
