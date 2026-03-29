package com.qsh.multiagent.domain.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class BuildReport {
    private boolean passed;// 是否通过
    private String buildLogSummary;// 构建日志摘要
    private String errorDetails;// 错误详情
}
