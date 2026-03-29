package com.qsh.multiagent.domain.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitTestReport {
    private boolean passed;// 是否通过
    private Integer totalCount;// 测试总数
    private Integer passedCount;// 通过数
    private Integer failedCount;// 失败数
    private String failureSummary;// 失败摘要
}
