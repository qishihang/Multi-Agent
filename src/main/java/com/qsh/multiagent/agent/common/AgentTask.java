package com.qsh.multiagent.agent.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentTask {
    private String taskId;
    private String planId;
    private Integer round;
    private AgentType targetAgentType;
    private String objective;
    private String input;
}
