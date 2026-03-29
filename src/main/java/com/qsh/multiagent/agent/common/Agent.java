package com.qsh.multiagent.agent.common;

public interface Agent {
    AgentType getType();
    AgentResult execute(AgentTask task);
}
