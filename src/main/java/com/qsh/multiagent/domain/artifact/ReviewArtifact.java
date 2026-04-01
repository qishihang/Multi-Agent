package com.qsh.multiagent.domain.artifact;

import com.qsh.multiagent.agent.common.AgentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReviewArtifact extends Artifact {

    private boolean passed;
    private Integer issueCount;
    private Integer blockingIssueCount;
    private String details;
    private String planAlignmentSummary;
    private String evidenceSummary;

    public ReviewArtifact(String artifactId,
                          String conversationId,
                          String taskId,
                          String runId,
                          Integer round,
                          AgentType producerAgentType,
                          Instant createdAt) {
        super(
                artifactId,
                ArtifactType.REVIEW,
                conversationId,
                taskId,
                runId,
                round,
                producerAgentType,
                nowIfNull(createdAt)
        );
    }
}
