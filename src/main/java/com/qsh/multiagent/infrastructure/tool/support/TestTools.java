package com.qsh.multiagent.infrastructure.tool.support;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.executor.WorkspaceCommandExecutor;
import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
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

    private final WorkspaceManager workspaceManager;
    private final WorkspaceCommandExecutor workspaceCommandExecutor;

    public TestTools(WorkspaceManager workspaceManager,
                     WorkspaceCommandExecutor workspaceCommandExecutor) {
        this.workspaceManager = workspaceManager;
        this.workspaceCommandExecutor = workspaceCommandExecutor;
    }

    @Tool(
            name = "detectProjectType",
            value = "检测当前工作空间的项目类型、构建工具和推荐的验证策略"
    )
    public String detectProjectType(@ToolMemoryId String conversationId) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        List<String> filePaths = workspaceManager.listFiles(conversation).stream()
                .map(entry -> entry.getRelativePath())
                .toList();

        Map<String, String> hints = new LinkedHashMap<>();
        if (filePaths.stream().anyMatch(path -> path.equals("pom.xml"))) {
            hints.put("projectType", "java-maven");
            hints.put("suggestedCompileCommand", "mvn -q -DskipTests compile");
            hints.put("suggestedTestCommand", "mvn -q test");
        } else if (filePaths.stream().anyMatch(path -> path.equals("build.gradle") || path.equals("build.gradle.kts"))) {
            hints.put("projectType", "java-gradle");
            hints.put("suggestedCompileCommand", "./gradlew compileJava");
            hints.put("suggestedTestCommand", "./gradlew test");
        } else if (filePaths.stream().anyMatch(path -> path.equals("package.json"))) {
            hints.put("projectType", "node");
            hints.put("suggestedCompileCommand", "none");
            hints.put("suggestedTestCommand", "npm test");
        } else if (filePaths.stream().anyMatch(path -> path.equals("pyproject.toml") || path.equals("requirements.txt"))) {
            hints.put("projectType", "python");
            hints.put("suggestedCompileCommand", "none");
            hints.put("suggestedTestCommand", "pytest");
        } else if (filePaths.stream().anyMatch(path -> path.equals("go.mod"))) {
            hints.put("projectType", "go");
            hints.put("suggestedCompileCommand", "none");
            hints.put("suggestedTestCommand", "go test ./...");
        } else if (filePaths.stream().anyMatch(path -> path.equals("Cargo.toml"))) {
            hints.put("projectType", "rust");
            hints.put("suggestedCompileCommand", "cargo check");
            hints.put("suggestedTestCommand", "cargo test");
        } else {
            hints.put("projectType", "unknown");
            hints.put("suggestedCompileCommand", "unknown");
            hints.put("suggestedTestCommand", "unknown");
        }

        return hints.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    @Tool(
            name = "writeWorkspaceFile",
            value = "向当前工作空间写入或覆盖文件，参数必须是相对路径"
    )
    public String writeWorkspaceFile(@P("相对于工作空间根目录的文件路径") String relativePath,
                                     @P("要写入的文件内容") String content,
                                     @ToolMemoryId String conversationId) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        workspaceManager.writeTextFile(conversation, relativePath, content);
        return "文件已写入: " + relativePath;
    }

    @Tool(
            name = "runVerificationCommand",
            value = "在当前工作空间中执行验证命令，例如编译或测试命令，并返回执行结果摘要"
    )
    public String runVerificationCommand(@P("要执行的命令，使用空格分隔，例如 mvn -q test") String command,
                                         @ToolMemoryId String conversationId) {
        Conversation conversation = workspaceManager.getConversationOrThrow(conversationId);
        List<String> commandParts = Arrays.stream(command.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .toList();

        validateCommand(commandParts);

        CommandExecutionResult result = workspaceCommandExecutor.execute(conversation, commandParts);
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

    private void validateCommand(List<String> commandParts) {
        if (commandParts.isEmpty()) {
            throw new IllegalArgumentException("Command must not be empty");
        }

        String executable = commandParts.get(0);
        List<String> allowedExecutables = List.of(
                "mvn", "./mvnw", "gradle", "./gradlew", "npm", "pnpm", "yarn",
                "pytest", "python", "python3", "go", "cargo"
        );

        if (!allowedExecutables.contains(executable)) {
            throw new IllegalArgumentException("Executable is not allowed: " + executable);
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
