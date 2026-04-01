package com.qsh.multiagent.domain.artifact;

import com.qsh.multiagent.agent.common.AgentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Artifact {

    private String artifactId;
    private ArtifactType artifactType;
    private String conversationId;
    private String taskId;
    private String runId;
    private Integer round;
    private AgentType producerAgentType;
    private Instant createdAt;

    protected static Instant nowIfNull(Instant createdAt) {
        return createdAt == null ? Instant.now() : createdAt;
    }
}
