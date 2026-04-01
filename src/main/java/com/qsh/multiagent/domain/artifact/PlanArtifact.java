package com.qsh.multiagent.domain.artifact;

import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.plan.PlanStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlanArtifact extends Artifact {

    private String objective;
    private String doneCriteria;
    private List<PlanStep> steps = new ArrayList<>();
    private List<AgentType> recommendedAgentRoles = new ArrayList<>();
    private List<AgentType> recommendedVerifiers = new ArrayList<>();
    private String stopConditionHint;
    private String replanStrategyHint;

    public PlanArtifact(String artifactId,
                        String conversationId,
                        String taskId,
                        String runId,
                        Integer round,
                        AgentType producerAgentType,
                        Instant createdAt) {
        super(
                artifactId,
                ArtifactType.PLAN,
                conversationId,
                taskId,
                runId,
                round,
                producerAgentType,
                nowIfNull(createdAt)
        );
    }

    public void addStep(PlanStep step) {
        if (step == null) {
            return;
        }
        if (steps == null) {
            steps = new ArrayList<>();
        }
        steps.add(step);
    }

    public void addRecommendedAgentRole(AgentType agentType) {
        if (agentType == null) {
            return;
        }
        if (recommendedAgentRoles == null) {
            recommendedAgentRoles = new ArrayList<>();
        }
        recommendedAgentRoles.add(agentType);
    }

    public void addRecommendedVerifier(AgentType agentType) {
        if (agentType == null) {
            return;
        }
        if (recommendedVerifiers == null) {
            recommendedVerifiers = new ArrayList<>();
        }
        recommendedVerifiers.add(agentType);
    }
}
