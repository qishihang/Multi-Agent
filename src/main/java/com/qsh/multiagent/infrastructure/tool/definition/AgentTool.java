package com.qsh.multiagent.infrastructure.tool.definition;

import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.infrastructure.tool.executor.ToolExecutionResult;

public interface AgentTool {

    String getName();

    ToolExecutionResult execute(AgentTask task);
}
