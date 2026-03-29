package com.qsh.multiagent.domain.report.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregatedResult {
    private String taskId;// 任务ID
    private String planId;// 计划ID
    private Integer round;// 轮次
    private boolean allPassed;// 是否全部通过
    private boolean hasBlockingIssues;// 是否有阻塞问题
    private String summary;// 总结
    private String suggestion;// 建议
}
