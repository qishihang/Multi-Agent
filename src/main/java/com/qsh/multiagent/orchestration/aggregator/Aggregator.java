package com.qsh.multiagent.orchestration.aggregator;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.domain.artifact.AggregateArtifact;

import java.util.List;

public interface Aggregator {
    AggregateArtifact aggregateArtifact(List<AgentResult> results);
}
