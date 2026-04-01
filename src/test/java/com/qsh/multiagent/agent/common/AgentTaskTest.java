package com.qsh.multiagent.agent.common;

import com.qsh.multiagent.domain.artifact.CodeArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.context.ExecutionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class AgentTaskTest {

    @Test
    void should_find_input_artifacts_by_type() {
        AgentTask agentTask = new AgentTask();
        agentTask.addInputArtifact(new PlanArtifact(
                "plan-1",
                "conv-1",
                "task-1",
                "run-1",
                1,
                AgentType.PLANNER,
                Instant.now()
        ));
        agentTask.addInputArtifact(new CodeArtifact(
                "code-1",
                "conv-1",
                "task-1",
                "run-1",
                1,
                AgentType.CODER,
                Instant.now()
        ));

        Assertions.assertNotNull(agentTask.findInputArtifact(PlanArtifact.class));
        Assertions.assertNotNull(agentTask.findInputArtifact(CodeArtifact.class));
    }

    @Test
    void should_expose_execution_context_shortcuts() {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setConversationId("conv-123");
        executionContext.setRunId("run-123");
        executionContext.setMemoryScope("conv-123::coder::task-1");

        AgentTask agentTask = new AgentTask();
        agentTask.setExecutionContext(executionContext);

        Assertions.assertEquals("conv-123", agentTask.getConversationId());
        Assertions.assertEquals("run-123", agentTask.getRunId());
        Assertions.assertEquals("conv-123::coder::task-1", agentTask.getMemoryScope());
    }
}
