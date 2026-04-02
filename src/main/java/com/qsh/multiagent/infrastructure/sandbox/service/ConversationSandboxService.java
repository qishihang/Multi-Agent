package com.qsh.multiagent.infrastructure.sandbox.service;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.conversation.ConversationRegistry;
import com.qsh.multiagent.infrastructure.executor.WorkspaceCommandExecutor;
import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.sandbox.policy.SandboxPolicy;
import com.qsh.multiagent.infrastructure.workspace.resolver.WorkspaceResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationSandboxService {

    private final ConversationRegistry conversationRegistry;
    private final WorkspaceResolver workspaceResolver;
    private final SandboxPolicy sandboxPolicy;
    private final WorkspaceCommandExecutor workspaceCommandExecutor;
    private final ConversationSandboxManager conversationSandboxManager;

    public ConversationSandboxService(ConversationRegistry conversationRegistry,
                                      WorkspaceResolver workspaceResolver,
                                      SandboxPolicy sandboxPolicy,
                                      WorkspaceCommandExecutor workspaceCommandExecutor,
                                      ConversationSandboxManager conversationSandboxManager) {
        this.conversationRegistry = conversationRegistry;
        this.workspaceResolver = workspaceResolver;
        this.sandboxPolicy = sandboxPolicy;
        this.workspaceCommandExecutor = workspaceCommandExecutor;
        this.conversationSandboxManager = conversationSandboxManager;
    }

    public CommandExecutionResult executeCommand(String conversationId, List<String> commandParts) {
        Conversation conversation = conversationRegistry.getConversationOrThrow(conversationId);
        var workspaceRoot = workspaceResolver.getWorkspaceRoot(conversationId);
        var sandboxContext = sandboxPolicy.buildContext(conversation, workspaceRoot);
        return workspaceCommandExecutor.execute(sandboxContext, commandParts);
    }

    public void releaseConversationSession(String conversationId) {
        conversationRegistry.getConversationOrThrow(conversationId);
        conversationSandboxManager.releaseConversationSession(conversationId);
    }
}
