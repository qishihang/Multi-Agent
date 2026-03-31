package com.qsh.multiagent.infrastructure.tool.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolExecutionResult {

    private String toolName;
    private boolean success;
    private String content;
}
