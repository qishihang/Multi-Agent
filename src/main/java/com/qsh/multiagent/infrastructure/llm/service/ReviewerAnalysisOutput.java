package com.qsh.multiagent.infrastructure.llm.service;

// 模型输出对象
public record ReviewerAnalysisOutput(Boolean passed,
                                     Integer issueCount,
                                     Integer blockingIssueCount,
                                     String details) {
}
