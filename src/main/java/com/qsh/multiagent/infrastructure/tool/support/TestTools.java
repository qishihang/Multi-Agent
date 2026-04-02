package com.qsh.multiagent.infrastructure.tool.support;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.conversation.ConversationRegistry;
import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.sandbox.model.ProjectRuntimeType;
import com.qsh.multiagent.infrastructure.sandbox.service.ConversationSandboxService;
import com.qsh.multiagent.infrastructure.sandbox.service.EnvironmentPreparationService;
import com.qsh.multiagent.infrastructure.sandbox.service.ProjectRuntimeResolver;
import com.qsh.multiagent.infrastructure.workspace.resolver.WorkspaceResolver;
import com.qsh.multiagent.infrastructure.workspace.service.WorkspaceFileService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TestTools {

    private final ConversationRegistry conversationRegistry;
    private final WorkspaceResolver workspaceResolver;
    private final WorkspaceFileService workspaceFileService;
    private final ConversationSandboxService conversationSandboxService;
    private final ProjectRuntimeResolver projectRuntimeResolver;
    private final EnvironmentPreparationService environmentPreparationService;


    public TestTools(ConversationRegistry conversationRegistry,
                     WorkspaceResolver workspaceResolver,
                     WorkspaceFileService workspaceFileService,
                     ConversationSandboxService conversationSandboxService,
                     ProjectRuntimeResolver projectRuntimeResolver,
                     EnvironmentPreparationService environmentPreparationService) {
        this.conversationRegistry = conversationRegistry;
        this.workspaceResolver = workspaceResolver;
        this.workspaceFileService = workspaceFileService;
        this.conversationSandboxService = conversationSandboxService;
        this.projectRuntimeResolver = projectRuntimeResolver;
        this.environmentPreparationService = environmentPreparationService;
    }

    @Tool(
            name = "detectProjectType",
            value = "检测当前工作空间的项目类型、构建工具和推荐的验证策略"
    )
    public String detectProjectType(@ToolMemoryId String memoryId) {
        Conversation conversation = conversationRegistry.getConversationByMemoryIdOrThrow(memoryId);
        var workspaceRoot = workspaceResolver.getWorkspaceRoot(conversation.getId());
        List<String> filePaths = workspaceFileService
                .listFiles(workspaceRoot).stream()
                .map(entry -> entry.getRelativePath())
                .toList();

        ProjectRuntimeType runtimeType = projectRuntimeResolver.resolveRuntimeType(workspaceRoot);
        Map<String, String> hints = new LinkedHashMap<>();
        hints.put("projectType", projectRuntimeResolver.describe(runtimeType));
        hints.put("suggestedCompileCommand", projectRuntimeResolver.suggestedCompileCommand(runtimeType));
        hints.put("suggestedTestCommand", projectRuntimeResolver.suggestedTestCommand(runtimeType));
        hints.put("workspaceFileCount", String.valueOf(filePaths.size()));

        return hints.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    @Tool(
            name = "prepareDependencies",
            value = "在当前会话沙箱中根据项目类型准备依赖环境，例如下载 Maven 依赖或安装 Node/Python 包"
    )
    public String prepareDependencies(@ToolMemoryId String memoryId) {
        Conversation conversation = conversationRegistry.getConversationByMemoryIdOrThrow(memoryId);
        var workspaceRoot = workspaceResolver.getWorkspaceRoot(conversation.getId());
        CommandExecutionResult result = environmentPreparationService.prepareDependencies(conversation.getId(), workspaceRoot);
        return """
                success: %s
                exitCode: %s
                stdout:
                %s

                stderr:
                %s
                """.formatted(
                result.isSuccess(),
                result.getExitCode(),
                truncate(result.getStdout(), 8000),
                truncate(result.getStderr(), 8000)
        );
    }

    @Tool(
            name = "prepareBuildEnvironment",
            value = "在当前会话沙箱中准备项目构建环境，例如预编译测试类、构建基础产物或校验运行时是否可构建"
    )
    public String prepareBuildEnvironment(@ToolMemoryId String memoryId) {
        Conversation conversation = conversationRegistry.getConversationByMemoryIdOrThrow(memoryId);
        var workspaceRoot = workspaceResolver.getWorkspaceRoot(conversation.getId());
        CommandExecutionResult result = environmentPreparationService.prepareBuildEnvironment(
                conversation.getId(),
                workspaceRoot
        );
        return """
                success: %s
                exitCode: %s
                stdout:
                %s

                stderr:
                %s
                """.formatted(
                result.isSuccess(),
                result.getExitCode(),
                truncate(result.getStdout(), 8000),
                truncate(result.getStderr(), 8000)
        );
    }

    @Tool(
            name = "runVerificationCommand",
            value = "在当前工作空间中执行验证命令，例如编译或测试命令，并返回执行结果摘要"
    )
    public String runVerificationCommand(@P("要执行的命令，使用空格分隔，例如 mvn -q test") String command,
                                         @ToolMemoryId String memoryId) {
        Conversation conversation = conversationRegistry.getConversationByMemoryIdOrThrow(memoryId);
        List<String> commandParts = Arrays.stream(command.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .toList();

        CommandExecutionResult result = conversationSandboxService.executeCommand(conversation.getId(), commandParts);
        return """
                success: %s
                exitCode: %s
                stdout:
                %s

                stderr:
                %s
                """.formatted(
                result.isSuccess(),
                result.getExitCode(),
                truncate(result.getStdout(), 8000),
                truncate(result.getStderr(), 8000)
        );
    }

    @Tool(
            name = "resetConversationSandbox",
            value = "重置当前会话对应的 Docker 沙箱，会销毁并在下一次命令执行时重新创建"
    )
    public String resetConversationSandbox(@ToolMemoryId String memoryId) {
        Conversation conversation = conversationRegistry.getConversationByMemoryIdOrThrow(memoryId);
        conversationSandboxService.releaseConversationSession(conversation.getId());
        return "会话沙箱已重置: " + conversation.getId();
    }

    private String truncate(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "\n... [truncated]";
    }
}
