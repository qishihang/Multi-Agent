package com.qsh.multiagent.infrastructure.sandbox.service;

import com.qsh.multiagent.infrastructure.executor.model.CommandExecutionResult;
import com.qsh.multiagent.infrastructure.sandbox.model.ProjectRuntimeType;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class EnvironmentPreparationService {

    private final ProjectRuntimeResolver projectRuntimeResolver;
    private final ConversationSandboxService conversationSandboxService;

    public EnvironmentPreparationService(ProjectRuntimeResolver projectRuntimeResolver,
                                         ConversationSandboxService conversationSandboxService) {
        this.projectRuntimeResolver = projectRuntimeResolver;
        this.conversationSandboxService = conversationSandboxService;
    }

    public CommandExecutionResult prepareDependencies(String conversationId, Path workspaceRoot) {
        ProjectRuntimeType runtimeType = projectRuntimeResolver.resolveRuntimeType(workspaceRoot);
        List<String> command = projectRuntimeResolver.prepareDependenciesCommand(runtimeType, workspaceRoot);

        return executePreparationCommand(conversationId, command, runtimeType, "dependency preparation");
    }

    public CommandExecutionResult prepareBuildEnvironment(String conversationId, Path workspaceRoot) {
        ProjectRuntimeType runtimeType = projectRuntimeResolver.resolveRuntimeType(workspaceRoot);
        List<String> command = projectRuntimeResolver.prepareBuildEnvironmentCommand(runtimeType, workspaceRoot);

        return executePreparationCommand(conversationId, command, runtimeType, "build environment preparation");
    }

    private CommandExecutionResult executePreparationCommand(String conversationId,
                                                             List<String> command,
                                                             ProjectRuntimeType runtimeType,
                                                             String phase) {
        if (command.isEmpty()) {
            return new CommandExecutionResult(
                    false,
                    -1,
                    "",
                    "No supported command for " + phase + ", runtime type: "
                            + projectRuntimeResolver.describe(runtimeType)
            );
        }

        return conversationSandboxService.executeCommand(conversationId, command);
    }
}
