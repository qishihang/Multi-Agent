package com.qsh.multiagent.domain.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoderReport {

    private boolean passed;
    private String changeSummary;
    private List<String> changedFiles;
    private String codeDraft;
    private String risks;
}
