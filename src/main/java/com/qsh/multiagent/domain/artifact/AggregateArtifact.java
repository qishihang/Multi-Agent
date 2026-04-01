package com.qsh.multiagent.domain.artifact;

import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AggregateArtifact extends Artifact {

    private boolean allPassed;
    private boolean hasBlockingIssues;
    private List<String> blockingIssues = new ArrayList<>();
    private List<String> nonBlockingIssues = new ArrayList<>();
    private List<String> failedValidations = new ArrayList<>();
    private String summary;
    private String plannerFeedback;
    private String nextRoundSuggestion;
    private TaskDecision recommendedDecision;

    public AggregateArtifact(String artifactId,
                             String conversationId,
                             String taskId,
                             String runId,
                             Integer round,
                             AgentType producerAgentType,
                             Instant createdAt) {
        super(
                artifactId,
                ArtifactType.AGGREGATE,
                conversationId,
                taskId,
                runId,
                round,
                producerAgentType,
                nowIfNull(createdAt)
        );
    }

    public void addBlockingIssue(String issue) {
        if (issue == null || issue.isBlank()) {
            return;
        }
        if (blockingIssues == null) {
            blockingIssues = new ArrayList<>();
        }
        blockingIssues.add(issue);
    }

    public void addNonBlockingIssue(String issue) {
        if (issue == null || issue.isBlank()) {
            return;
        }
        if (nonBlockingIssues == null) {
            nonBlockingIssues = new ArrayList<>();
        }
        nonBlockingIssues.add(issue);
    }

    public void addFailedValidation(String validation) {
        if (validation == null || validation.isBlank()) {
            return;
        }
        if (failedValidations == null) {
            failedValidations = new ArrayList<>();
        }
        failedValidations.add(validation);
    }
}
