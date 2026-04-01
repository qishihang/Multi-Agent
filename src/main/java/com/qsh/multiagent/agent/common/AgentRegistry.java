package com.qsh.multiagent.agent.common;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AgentRegistry {

    private final Map<AgentType, Agent> agentMap;

    public AgentRegistry(List<Agent> agents) {
        this.agentMap = new EnumMap<>(AgentType.class);
        for (Agent agent : agents) {
            Agent previous = this.agentMap.put(agent.getType(), agent);
            if (previous != null) {
                throw new IllegalStateException("Duplicate agent registered for type: " + agent.getType());
            }
        }
    }

    public Agent getRequiredAgent(AgentType agentType) {
        Agent agent = agentMap.get(agentType);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found for type: " + agentType);
        }
        return agent;
    }

    public List<Agent> getVerifierAgents() {
        return agentMap.values().stream()
                .filter(agent -> agent.getType() == AgentType.REVIEWER || agent.getType() == AgentType.TESTER)
                .collect(Collectors.toList());
    }
}
