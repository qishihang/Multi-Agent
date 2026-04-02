package com.qsh.multiagent.infrastructure.tool.support;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.conversation.ConversationRegistry;
import com.qsh.multiagent.infrastructure.sandbox.policy.SandboxPolicy;
import com.qsh.multiagent.infrastructure.workspace.resolver.WorkspaceResolver;
import com.qsh.multiagent.infrastructure.workspace.service.WorkspaceFileService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceWriteTools {

    private final ConversationRegistry conversationRegistry;
    private final WorkspaceResolver workspaceResolver;
    private final WorkspaceFileService workspaceFileService;
    private final SandboxPolicy sandboxPolicy;

    public WorkspaceWriteTools(ConversationRegistry conversationRegistry,
                               WorkspaceResolver workspaceResolver,
                               WorkspaceFileService workspaceFileService,
                               SandboxPolicy sandboxPolicy) {
        this.conversationRegistry = conversationRegistry;
        this.workspaceResolver = workspaceResolver;
        this.workspaceFileService = workspaceFileService;
        this.sandboxPolicy = sandboxPolicy;
    }

    @Tool(
            name = "writeWorkspaceFile",
            value = "向当前工作空间写入或覆盖文件，参数必须是相对路径"
    )
    public String writeWorkspaceFile(@P("相对于工作空间根目录的文件路径") String relativePath,
                                     @P("要写入的文件内容") String content,
                                     @ToolMemoryId String memoryId) {
        Conversation conversation = conversationRegistry.getConversationByMemoryIdOrThrow(memoryId);

        var workspaceRoot = workspaceResolver.getWorkspaceRoot(conversation.getId());
        var sandboxContext = sandboxPolicy.buildContext(conversation, workspaceRoot);
        var target = workspaceRoot.resolve(relativePath).normalize();

        sandboxPolicy.validateWritePath(sandboxContext, target);
        workspaceFileService.writeTextFile(workspaceRoot, relativePath, content);

        return "文件已写入: " + relativePath;
    }
}
