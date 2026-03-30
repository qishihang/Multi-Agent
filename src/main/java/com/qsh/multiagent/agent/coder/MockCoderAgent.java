package com.qsh.multiagent.agent.coder;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;

public class MockCoderAgent implements Agent {

    @Override
    public AgentType getType() {
        return AgentType.CODER;
    }

    @Override
    public AgentResult<?> execute(AgentTask task) {

        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                true,
                "Mock coder executed successfully.",
                null,
                null
        );
    }
}
