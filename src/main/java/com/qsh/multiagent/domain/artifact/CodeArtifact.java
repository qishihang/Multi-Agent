package com.qsh.multiagent.domain.artifact;

import com.qsh.multiagent.agent.common.AgentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CodeArtifact extends Artifact {

    private boolean filesWritten;
    private String changeSummary;
    private List<String> changedFiles = new ArrayList<>();
    private String codeDraft;
    private String risks;
    private String reviewFocusHint;
    private String testFocusHint;

    public CodeArtifact(String artifactId,
                        String conversationId,
                        String taskId,
                        String runId,
                        Integer round,
                        AgentType producerAgentType,
                        Instant createdAt) {
        super(
                artifactId,
                ArtifactType.CODE,
                conversationId,
                taskId,
                runId,
                round,
                producerAgentType,
                nowIfNull(createdAt)
        );
    }

    public void addChangedFile(String changedFile) {
        if (changedFile == null || changedFile.isBlank()) {
            return;
        }
        if (changedFiles == null) {
            changedFiles = new ArrayList<>();
        }
        changedFiles.add(changedFile);
    }
}
