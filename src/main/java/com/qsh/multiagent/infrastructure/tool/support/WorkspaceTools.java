package com.qsh.multiagent.infrastructure.tool.support;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.sandbox.policy.SandboxPolicy;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WorkspaceTools {

    private final WorkspaceManager workspaceManager;
    private final SandboxPolicy sandboxPolicy;

    public WorkspaceTools(WorkspaceManager workspaceManager,
                          SandboxPolicy sandboxPolicy) {
        this.workspaceManager = workspaceManager;
        this.sandboxPolicy = sandboxPolicy;
    }

    @Tool(
            name = "listWorkspaceFiles",
            value = "列出当前会话工作空间中的文件和目录，返回相对路径列表"
    )
    public String listFiles(@ToolMemoryId String conversationId) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        Path root = workspaceManager.getWorkspaceRoot(conversation);
        var sandboxContext = sandboxPolicy.buildContext(conversation, root);
        sandboxPolicy.validateAccessPath(sandboxContext, root);

        try (Stream<Path> stream = Files.walk(root)) {
            List<String> entries = stream
                    .filter(path -> !path.equals(root))
                    .map(path -> root.relativize(path).toString())
                    .sorted()
                    .limit(200)
                    .toList();

            if (entries.isEmpty()) {
                return "当前工作空间为空。";
            }

            return entries.stream().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list files for conversation: " + conversationId, e);
        }
    }

    @Tool(
            name = "readWorkspaceFile",
            value = "读取当前会话工作空间中的指定文件内容，参数必须是相对路径"
    )
    public String readFile(@P("相对于工作空间根目录的文件路径") String relativePath,
                           @ToolMemoryId String conversationId) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        Path root = workspaceManager.getWorkspaceRoot(conversation);
        Path target = root.resolve(relativePath).normalize();
        workspaceManager.ensureInsideWorkspace(root, target);
        var sandboxContext = sandboxPolicy.buildContext(conversation, root);
        sandboxPolicy.validateAccessPath(sandboxContext, target);

        try {
            if (!Files.exists(target) || Files.isDirectory(target)) {
                return "文件不存在或路径指向目录: " + relativePath;
            }
            String content = Files.readString(target, StandardCharsets.UTF_8);
            return truncate(content, 12000);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file: " + relativePath, e);
        }
    }

    @Tool(
            name = "searchWorkspaceCode",
            value = "在当前会话工作空间中搜索包含指定关键词的文件，返回相对路径列表"
    )
    public String searchCode(@P("要搜索的关键词") String keyword,
                             @ToolMemoryId String conversationId) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        Path root = workspaceManager.getWorkspaceRoot(conversation);
        var sandboxContext = sandboxPolicy.buildContext(conversation, root);
        sandboxPolicy.validateAccessPath(sandboxContext, root);

        try (Stream<Path> stream = Files.walk(root)) {
            List<String> matches = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> containsKeyword(path, keyword))
                    .map(path -> root.relativize(path).toString())
                    .limit(100)
                    .toList();

            if (matches.isEmpty()) {
                return "未找到关键词: " + keyword;
            }

            return String.join("\n", matches);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to search code: " + keyword, e);
        }
    }

    private boolean containsKeyword(Path path, String keyword) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return content.contains(keyword);
        } catch (IOException e) {
            return false;
        }
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
