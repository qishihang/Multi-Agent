package com.qsh.multiagent.infrastructure.executor;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;

import java.util.List;

public interface WorkspaceCommandExecutor { // 工作区命令执行器接口

    CommandExecutionResult execute(Conversation conversation, List<String> command);
}
