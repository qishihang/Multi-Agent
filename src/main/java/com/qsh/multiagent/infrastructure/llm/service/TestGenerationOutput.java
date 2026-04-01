package com.qsh.multiagent.infrastructure.llm.service;

import java.util.List;

public record TestGenerationOutput(
        Boolean passed,
        String projectType,
        Boolean compileRequired,
        Boolean compilePassed,
        Boolean testsGenerated,
        Integer generatedTestFileCount,
        Boolean testsExecuted,
        Integer testsPassedCount,
        Integer testsFailedCount,
        List<String> producedFiles,
        String summary,
        String failureAnalysis
) {
}
