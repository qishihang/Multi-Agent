package com.qsh.multiagent.infrastructure.executor;

import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.sandbox.model.SandboxContext;

import java.util.List;

public interface WorkspaceCommandExecutor { // 工作区命令执行器接口

    CommandExecutionResult execute(SandboxContext sandboxContext, List<String> command);
}
