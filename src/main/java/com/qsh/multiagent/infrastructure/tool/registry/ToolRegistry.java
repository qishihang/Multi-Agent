package com.qsh.multiagent.infrastructure.tool.registry;

import com.qsh.multiagent.infrastructure.tool.definition.AgentTool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolRegistry {

    private final List<AgentTool> agentTools;

    public ToolRegistry(List<AgentTool> agentTools) {
        this.agentTools = agentTools;
    }

    public List<AgentTool> getTools() {
        return agentTools;
    }
}
