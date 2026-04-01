package com.qsh.multiagent.agent.common;

import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.report.model.CoderReport;
import com.qsh.multiagent.domain.task.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentTask { // 用在Reviewer和Tester中
    private String taskId;
    private String planId;
    private Integer round;
    private AgentType targetAgentType;
    private String objective;
    private String input;

    private Task task;
    private Plan plan;
    private CoderReport coderReport;
}
