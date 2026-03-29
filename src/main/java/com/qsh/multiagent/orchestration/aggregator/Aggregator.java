package com.qsh.multiagent.orchestration.aggregator;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.domain.report.model.AggregatedResult;

import java.util.List;

public interface Aggregator {
    AggregatedResult aggregate(List<AgentResult<?>> results);
//    boolean allPassed(List<AgentResult> results);
}
