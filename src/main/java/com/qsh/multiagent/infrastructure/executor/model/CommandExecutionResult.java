package com.qsh.multiagent.infrastructure.executor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandExecutionResult { // 命令执行结果对象

    private boolean success;
    private int exitCode;
    private String stdout;
    private String stderr;
}
