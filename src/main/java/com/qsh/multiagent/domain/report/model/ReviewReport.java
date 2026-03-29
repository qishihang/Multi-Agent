package com.qsh.multiagent.domain.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ReviewReport {
    private boolean passed;// 是否通过
    private Integer issueCount;// 问题数量
    private Integer blockingIssueCount;// 阻塞问题数量
    private String details;// 详细描述
}
