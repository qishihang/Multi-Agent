package com.qsh.multiagent.agent.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentResult<T> {
    private String taskId;
    private String planId;
    private Integer round;
    private AgentType agentType;
    private boolean success;
    private String summary;
    private T data;
    private String errorMessage;

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
