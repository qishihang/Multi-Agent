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
public class TestArtifact extends Artifact {

    private boolean passed;
    private String projectType;
    private boolean compileRequired;
    private boolean compilePassed;
    private boolean testsGenerated;
    private Integer generatedTestFileCount;
    private boolean testsExecuted;
    private Integer testsPassedCount;
    private Integer testsFailedCount;
    private List<String> producedFiles = new ArrayList<>();
    private List<String> executedCommands = new ArrayList<>();
    private String summary;
    private String failureAnalysis;
    private String evidenceSummary;

    public TestArtifact(String artifactId,
                        String conversationId,
                        String taskId,
                        String runId,
                        Integer round,
                        AgentType producerAgentType,
                        Instant createdAt) {
        super(
                artifactId,
                ArtifactType.TEST,
                conversationId,
                taskId,
                runId,
                round,
                producerAgentType,
                nowIfNull(createdAt)
        );
    }

    public void addProducedFile(String producedFile) {
        if (producedFile == null || producedFile.isBlank()) {
            return;
        }
        if (producedFiles == null) {
            producedFiles = new ArrayList<>();
        }
        producedFiles.add(producedFile);
    }

    public void addExecutedCommand(String command) {
        if (command == null || command.isBlank()) {
            return;
        }
        if (executedCommands == null) {
            executedCommands = new ArrayList<>();
        }
        executedCommands.add(command);
    }
}
