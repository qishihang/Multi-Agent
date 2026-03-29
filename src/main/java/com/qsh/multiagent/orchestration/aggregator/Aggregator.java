package com.qsh.multiagent.orchestration.aggregator;

import com.qsh.multiagent.agent.common.AgentResult;

import java.util.List;

public interface Aggregator {
    String aggregate(List<AgentResult> results);
//    boolean allPassed(List<AgentResult> results);
}
