package com.qsh.multiagent.domain.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestReport {

    private boolean passed;
    private String projectType;
    private boolean compileRequired;
    private boolean compilePassed;
    private boolean testsGenerated;
    private Integer generatedTestFileCount;
    private boolean testsExecuted;
    private Integer testsPassedCount;
    private Integer testsFailedCount;
    private List<String> producedFiles;
    private String summary;
    private String failureAnalysis;
}
