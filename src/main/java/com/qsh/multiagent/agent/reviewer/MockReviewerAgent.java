package com.qsh.multiagent.agent.reviewer;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.ReviewArtifact;
import org.springframework.stereotype.Component;

import java.time.Instant;

//@Component
public class MockReviewerAgent implements Agent {

    @Override
    public AgentType getType() {
        return AgentType.REVIEWER;
    }

    @Override
    public AgentResult execute(AgentTask task) {
        AgentResult result = new AgentResult(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                true,
                "Reviewer passed.",
                null,
                null,
                null,
                null
        );
        ReviewArtifact artifact = new ReviewArtifact(
                "mock-review-" + task.getTaskId() + "-" + task.getRound(),
                task.getExecutionContext() != null ? task.getExecutionContext().getConversationId() : null,
                task.getTaskId(),
                task.getExecutionContext() != null ? task.getExecutionContext().getRunId() : null,
                task.getRound(),
                AgentType.REVIEWER,
                Instant.now()
        );
        artifact.setPassed(true);
        artifact.setIssueCount(0);
        artifact.setBlockingIssueCount(0);
        artifact.setDetails("Mock review passed.");
        result.addOutputArtifact(artifact);
        return result;
    }
}
