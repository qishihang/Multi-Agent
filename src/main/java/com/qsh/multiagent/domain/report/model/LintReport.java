package com.qsh.multiagent.domain.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LintReport {
    private boolean passed;// 是否通过
    private Integer warningCount;// 警告数
    private Integer errorCount;// 错误数
    private String details;// 详情
}
