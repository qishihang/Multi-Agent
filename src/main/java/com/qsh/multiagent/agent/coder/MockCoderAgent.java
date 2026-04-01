package com.qsh.multiagent.agent.coder;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.CodeArtifact;
import org.springframework.stereotype.Component;

import java.time.Instant;

//@Component
public class MockCoderAgent implements Agent {

    @Override
    public AgentType getType() {
        return AgentType.CODER;
    }

    @Override
    public AgentResult execute(AgentTask task) {
        AgentResult result = new AgentResult(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                true,
                "Mock coder executed successfully.",
                null,
                null,
                null,
                null
        );
        result.addOutputArtifact(new CodeArtifact(
                "mock-code-" + task.getTaskId() + "-" + task.getRound(),
                task.getExecutionContext() != null ? task.getExecutionContext().getConversationId() : null,
                task.getTaskId(),
                task.getExecutionContext() != null ? task.getExecutionContext().getRunId() : null,
                task.getRound(),
                AgentType.CODER,
                Instant.now()
        ));
        return result;
    }
}
