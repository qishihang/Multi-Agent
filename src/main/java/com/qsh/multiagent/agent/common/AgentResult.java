package com.qsh.multiagent.agent.common;

import com.qsh.multiagent.domain.artifact.Artifact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentResult {
    private String taskId;
    private String planId;
    private Integer round;
    private AgentType agentType;
    private boolean success;
    private String summary;
    private String errorMessage;
    private List<Artifact> outputArtifacts = new ArrayList<>();
    private List<String> issues = new ArrayList<>();
    private String rawEvidence;

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }

    public void addOutputArtifact(Artifact artifact) {
        if (artifact == null) {
            return;
        }
        if (outputArtifacts == null) {
            outputArtifacts = new ArrayList<>();
        }
        outputArtifacts.add(artifact);
    }

    public void addIssue(String issue) {
        if (issue == null || issue.isBlank()) {
            return;
        }
        if (issues == null) {
            issues = new ArrayList<>();
        }
        issues.add(issue);
    }
}
