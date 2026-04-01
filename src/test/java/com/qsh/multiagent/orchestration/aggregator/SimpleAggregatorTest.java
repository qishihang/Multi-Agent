package com.qsh.multiagent.orchestration.aggregator;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.AggregateArtifact;
import com.qsh.multiagent.domain.artifact.ReviewArtifact;
import com.qsh.multiagent.domain.artifact.TestArtifact;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class SimpleAggregatorTest {

    private final SimpleAggregator aggregator = new SimpleAggregator();

    @Test
    void should_return_blocking_aggregate_when_no_results_exist() {
        AggregateArtifact aggregateArtifact = aggregator.aggregateArtifact(List.of());

        Assertions.assertFalse(aggregateArtifact.isAllPassed());
        Assertions.assertTrue(aggregateArtifact.isHasBlockingIssues());
        Assertions.assertEquals(TaskDecision.CONTINUE, aggregateArtifact.getRecommendedDecision());
        Assertions.assertFalse(aggregateArtifact.getBlockingIssues().isEmpty());
    }

    @Test
    void should_mark_finish_when_review_and_test_artifacts_pass() {
        ReviewArtifact reviewArtifact = new ReviewArtifact(
                "review-1",
                "conv-1",
                "task-1",
                "run-1",
                1,
                AgentType.REVIEWER,
                Instant.now()
        );
        reviewArtifact.setPassed(true);
        reviewArtifact.setIssueCount(0);
        reviewArtifact.setBlockingIssueCount(0);
        reviewArtifact.setDetails("Review passed.");

        TestArtifact testArtifact = new TestArtifact(
                "test-1",
                "conv-1",
                "task-1",
                "run-1",
                1,
                AgentType.TESTER,
                Instant.now()
        );
        testArtifact.setPassed(true);
        testArtifact.setCompileRequired(true);
        testArtifact.setCompilePassed(true);
        testArtifact.setTestsExecuted(true);
        testArtifact.setTestsPassedCount(3);
        testArtifact.setTestsFailedCount(0);
        testArtifact.setSummary("Tests passed.");

        AgentResult reviewerResult = new AgentResult(
                "task-1",
                "plan-1",
                1,
                AgentType.REVIEWER,
                true,
                "review summary",
                null,
                List.of(reviewArtifact),
                List.of(),
                null
        );

        AgentResult testerResult = new AgentResult(
                "task-1",
                "plan-1",
                1,
                AgentType.TESTER,
                true,
                "test summary",
                null,
                List.of(testArtifact),
                List.of(),
                null
        );

        AggregateArtifact aggregateArtifact = aggregator.aggregateArtifact(List.of(reviewerResult, testerResult));

        Assertions.assertTrue(aggregateArtifact.isAllPassed());
        Assertions.assertFalse(aggregateArtifact.isHasBlockingIssues());
        Assertions.assertEquals(TaskDecision.FINISH, aggregateArtifact.getRecommendedDecision());
        Assertions.assertEquals("conv-1", aggregateArtifact.getConversationId());
        Assertions.assertEquals("run-1", aggregateArtifact.getRunId());
    }

    @Test
    void should_continue_when_blocking_review_or_failed_tests_exist() {
        ReviewArtifact reviewArtifact = new ReviewArtifact(
                "review-2",
                "conv-2",
                "task-2",
                "run-2",
                2,
                AgentType.REVIEWER,
                Instant.now()
        );
        reviewArtifact.setPassed(false);
        reviewArtifact.setIssueCount(2);
        reviewArtifact.setBlockingIssueCount(1);
        reviewArtifact.setDetails("Found blocking issue.");

        TestArtifact testArtifact = new TestArtifact(
                "test-2",
                "conv-2",
                "task-2",
                "run-2",
                2,
                AgentType.TESTER,
                Instant.now()
        );
        testArtifact.setPassed(false);
        testArtifact.setCompileRequired(true);
        testArtifact.setCompilePassed(false);
        testArtifact.setTestsExecuted(true);
        testArtifact.setTestsPassedCount(1);
        testArtifact.setTestsFailedCount(2);
        testArtifact.setSummary("Tests failed.");
        testArtifact.setFailureAnalysis("Compilation and validation failed.");

        AgentResult reviewerResult = new AgentResult(
                "task-2",
                "plan-2",
                2,
                AgentType.REVIEWER,
                false,
                "review summary",
                null,
                List.of(reviewArtifact),
                List.of("Blocking review issue"),
                null
        );

        AgentResult testerResult = new AgentResult(
                "task-2",
                "plan-2",
                2,
                AgentType.TESTER,
                false,
                "test summary",
                "validation failed",
                List.of(testArtifact),
                List.of("Tests failed"),
                null
        );

        AggregateArtifact aggregateArtifact = aggregator.aggregateArtifact(List.of(reviewerResult, testerResult));

        Assertions.assertFalse(aggregateArtifact.isAllPassed());
        Assertions.assertTrue(aggregateArtifact.isHasBlockingIssues());
        Assertions.assertEquals(TaskDecision.CONTINUE, aggregateArtifact.getRecommendedDecision());
        Assertions.assertFalse(aggregateArtifact.getBlockingIssues().isEmpty());
        Assertions.assertFalse(aggregateArtifact.getFailedValidations().isEmpty());
    }
}
