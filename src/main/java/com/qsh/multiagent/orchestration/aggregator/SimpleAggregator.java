package com.qsh.multiagent.orchestration.aggregator;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.AggregateArtifact;
import com.qsh.multiagent.domain.artifact.Artifact;
import com.qsh.multiagent.domain.artifact.ReviewArtifact;
import com.qsh.multiagent.domain.artifact.TestArtifact;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleAggregator implements Aggregator{

    @Override
    public AggregateArtifact aggregateArtifact(List<AgentResult> results) {
        if (results == null || results.isEmpty()) {
            AggregateArtifact artifact = new AggregateArtifact(
                    "aggregate-empty",
                    null,
                    null,
                    null,
                    null,
                    AgentType.AGGREGATOR,
                    Instant.now()
            );
            artifact.setAllPassed(false);
            artifact.setHasBlockingIssues(true);
            artifact.addBlockingIssue("No verification results were produced.");
            artifact.setSummary("No verification results were produced.");
            artifact.setPlannerFeedback("Verification stage returned no usable outputs.");
            artifact.setNextRoundSuggestion("Check dispatcher and verifier agent execution.");
            artifact.setRecommendedDecision(TaskDecision.CONTINUE);
            return artifact;
        }

        AgentResult firstResult = results.get(0);
        AggregateArtifact artifact = new AggregateArtifact(
                buildAggregateArtifactId(firstResult),
                extractConversationId(results),
                firstResult.getTaskId(),
                extractRunId(results),
                firstResult.getRound(),
                AgentType.AGGREGATOR,
                Instant.now()
        );

        boolean allPassed = true;
        boolean hasBlockingIssues = false;
        List<String> summaryLines = new ArrayList<>();

        for (AgentResult result : results) {
            ReviewArtifact reviewArtifact = findArtifact(result, ReviewArtifact.class);
            if (reviewArtifact != null) {
                handleReviewArtifact(artifact, reviewArtifact, summaryLines);
            }

            TestArtifact testArtifact = findArtifact(result, TestArtifact.class);
            if (testArtifact != null) {
                handleTestArtifact(artifact, testArtifact, summaryLines);
            }

            if (!result.isSuccess()) {
                allPassed = false;
            }

            if (result.getIssues() != null) {
                for (String issue : result.getIssues()) {
                    artifact.addNonBlockingIssue("%s: %s".formatted(result.getAgentType(), issue));
                }
            }

            if ((reviewArtifact == null && testArtifact == null) && result.getSummary() != null && !result.getSummary().isBlank()) {
                summaryLines.add(result.getAgentType() + ": " + result.getSummary());
            }
        }

        if (!artifact.getBlockingIssues().isEmpty()) {
            hasBlockingIssues = true;
            allPassed = false;
        }
        if (!artifact.getFailedValidations().isEmpty()) {
            allPassed = false;
        }

        artifact.setAllPassed(allPassed);
        artifact.setHasBlockingIssues(hasBlockingIssues);
        artifact.setSummary(String.join(" | ", summaryLines));
        artifact.setPlannerFeedback(buildPlannerFeedback(artifact));
        artifact.setNextRoundSuggestion(buildNextRoundSuggestion(artifact));
        artifact.setRecommendedDecision(buildRecommendedDecision(artifact));
        return artifact;
    }

    private void handleReviewArtifact(AggregateArtifact aggregateArtifact,
                                      ReviewArtifact reviewArtifact,
                                      List<String> summaryLines) {
        summaryLines.add("REVIEWER: " + safe(reviewArtifact.getDetails()));

        Integer blockingCount = reviewArtifact.getBlockingIssueCount() == null ? 0 : reviewArtifact.getBlockingIssueCount();
        Integer issueCount = reviewArtifact.getIssueCount() == null ? 0 : reviewArtifact.getIssueCount();

        if (blockingCount > 0) {
            aggregateArtifact.addBlockingIssue("Reviewer reported %s blocking issues.".formatted(blockingCount));
        }
        if (issueCount > blockingCount) {
            aggregateArtifact.addNonBlockingIssue(
                    "Reviewer reported %s non-blocking issues.".formatted(issueCount - blockingCount)
            );
        }
        if (reviewArtifact.getPlanAlignmentSummary() != null && !reviewArtifact.getPlanAlignmentSummary().isBlank()) {
            aggregateArtifact.addNonBlockingIssue(reviewArtifact.getPlanAlignmentSummary());
        }
    }

    private void handleTestArtifact(AggregateArtifact aggregateArtifact,
                                    TestArtifact testArtifact,
                                    List<String> summaryLines) {
        summaryLines.add("TESTER: " + safe(testArtifact.getSummary()));

        Integer failedCount = testArtifact.getTestsFailedCount() == null ? 0 : testArtifact.getTestsFailedCount();
        if (failedCount > 0) {
            aggregateArtifact.addFailedValidation("Tester reported %s failed tests.".formatted(failedCount));
        }
        if (testArtifact.isDependencyPreparationAttempted() && !testArtifact.isDependencyPreparationPassed()) {
            aggregateArtifact.addFailedValidation("Dependency preparation did not pass.");
        }
        if (testArtifact.isCompileRequired() && !testArtifact.isCompilePassed()) {
            aggregateArtifact.addFailedValidation("Compilation did not pass.");
        }
        if (testArtifact.getFailureAnalysis() != null && !testArtifact.getFailureAnalysis().isBlank()) {
            aggregateArtifact.addNonBlockingIssue(testArtifact.getFailureAnalysis());
        }
    }

    private <T extends Artifact> T findArtifact(AgentResult result, Class<T> artifactClass) {
        if (result == null || result.getOutputArtifacts() == null) {
            return null;
        }
        for (Artifact artifact : result.getOutputArtifacts()) {
            if (artifactClass.isInstance(artifact)) {
                return artifactClass.cast(artifact);
            }
        }
        return null;
    }

    private String buildPlannerFeedback(AggregateArtifact artifact) {
        if (artifact.isAllPassed()) {
            return "Verification artifacts indicate the current round satisfies review and test expectations.";
        }
        if (artifact.isHasBlockingIssues()) {
            return "Verification artifacts contain blocking issues and require a corrective planning round.";
        }
        return "Verification artifacts contain follow-up issues; planner should decide whether to continue or replan.";
    }

    private String buildNextRoundSuggestion(AggregateArtifact artifact) {
        if (artifact.isAllPassed()) {
            return "Finish current task.";
        }
        if (artifact.isHasBlockingIssues()) {
            return "Continue to the next round and address blocking review findings first.";
        }
        if (!artifact.getFailedValidations().isEmpty()) {
            return "Continue to the next round and fix failed validations before expanding scope.";
        }
        return "Continue to the next round and refine the implementation based on verifier feedback.";
    }

    private com.qsh.multiagent.domain.workflow.TaskDecision buildRecommendedDecision(AggregateArtifact artifact) {
        if (artifact.isAllPassed()) {
            return com.qsh.multiagent.domain.workflow.TaskDecision.FINISH;
        }
        if (artifact.isHasBlockingIssues()) {
            return com.qsh.multiagent.domain.workflow.TaskDecision.CONTINUE;
        }
        if (!artifact.getFailedValidations().isEmpty()) {
            return com.qsh.multiagent.domain.workflow.TaskDecision.CONTINUE;
        }
        return com.qsh.multiagent.domain.workflow.TaskDecision.CONTINUE;
    }

    private String buildAggregateArtifactId(AgentResult firstResult) {
        return "aggregate-" + firstResult.getTaskId() + "-" + firstResult.getRound();
    }

    private String extractConversationId(List<AgentResult> results) {
        for (AgentResult result : results) {
            if (result.getOutputArtifacts() == null) {
                continue;
            }
            for (Artifact artifact : result.getOutputArtifacts()) {
                if (artifact.getConversationId() != null && !artifact.getConversationId().isBlank()) {
                    return artifact.getConversationId();
                }
            }
        }
        return null;
    }

    private String extractRunId(List<AgentResult> results) {
        for (AgentResult result : results) {
            if (result.getOutputArtifacts() == null) {
                continue;
            }
            for (Artifact artifact : result.getOutputArtifacts()) {
                if (artifact.getRunId() != null && !artifact.getRunId().isBlank()) {
                    return artifact.getRunId();
                }
            }
        }
        return null;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "No summary available." : value;
    }
}
