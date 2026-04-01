package com.qsh.multiagent.orchestration.dispatcher;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentRegistry;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.Artifact;
import com.qsh.multiagent.domain.artifact.CodeArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.context.ExecutionSandboxPolicy;
import com.qsh.multiagent.domain.context.ExecutionContext;
import com.qsh.multiagent.domain.context.ToolCapability;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.workflow.WorkflowRun;
import com.qsh.multiagent.infrastructure.workspace.manager.WorkspaceManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Component
public class LocalDispatcher implements Dispatcher{

    private final AgentRegistry agentRegistry;
    private final Executor verifierExecutor;
    private final WorkspaceManager workspaceManager;

    public LocalDispatcher(AgentRegistry agentRegistry,
                           @Qualifier("verifierExecutor") Executor verifierExecutor,
                           WorkspaceManager workspaceManager) {
        this.agentRegistry = agentRegistry;
        this.verifierExecutor = verifierExecutor;
        this.workspaceManager = workspaceManager;
    }

    @Override
    public AgentResult dispatchToCoder(Task task, WorkflowRun workflowRun, PlanArtifact planArtifact){
        AgentTask agentTask = buildBaseTask(task, workflowRun, planArtifact, AgentType.CODER, "Structured coding input");
        agentTask.addInputArtifact(planArtifact);
        Agent coderAgent = agentRegistry.getRequiredAgent(AgentType.CODER);
        return coderAgent.execute(agentTask);
    }

    @Override
    public List<AgentResult> dispatchToVerification(Task task,
                                                    WorkflowRun workflowRun,
                                                    PlanArtifact planArtifact,
                                                    AgentResult coderResult){
        List<CompletableFuture<AgentResult>> futures = new ArrayList<>();
        CodeArtifact codeArtifact = findArtifact(coderResult, CodeArtifact.class);

        AgentTask reviewerTask = buildBaseTask(task, workflowRun, planArtifact, AgentType.REVIEWER, "Structured review input");
        reviewerTask.addInputArtifact(planArtifact);
        if (codeArtifact != null) {
            reviewerTask.addInputArtifact(codeArtifact);
        }

        AgentTask testTask = buildBaseTask(task, workflowRun, planArtifact, AgentType.TESTER, "Structured test input");
        testTask.addInputArtifact(planArtifact);
        if (codeArtifact != null) {
            testTask.addInputArtifact(codeArtifact);
        }

        for (Agent verifierAgent : agentRegistry.getVerifierAgents()) {
            if (verifierAgent.getType() == AgentType.REVIEWER) {
                futures.add(executeVerifierAsync(verifierAgent, reviewerTask));
            } else if (verifierAgent.getType() == AgentType.TESTER) {
                futures.add(executeVerifierAsync(verifierAgent, testTask));
            }
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private AgentTask buildBaseTask(Task task,
                                    WorkflowRun workflowRun,
                                    PlanArtifact planArtifact,
                                    AgentType targetAgentType,
                                    String input) {
        AgentTask agentTask = new AgentTask();
        agentTask.setTaskId(task.getId());
        agentTask.setPlanId(planArtifact.getArtifactId());
        agentTask.setRound(planArtifact.getRound());
        agentTask.setTargetAgentType(targetAgentType);
        agentTask.setObjective(planArtifact.getObjective());
        agentTask.setTaskGoal(task.getGoal());
        agentTask.setInput(input);
        agentTask.setExecutionContext(buildExecutionContext(task, workflowRun, targetAgentType));
        return agentTask;
    }

    private ExecutionContext buildExecutionContext(Task task,
                                                   WorkflowRun workflowRun,
                                                   AgentType targetAgentType) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setConversationId(task.getConversationId());
        executionContext.setTaskId(task.getId());
        executionContext.setWorkspaceRoot(resolveWorkspaceRoot(task));
        executionContext.setRunId(workflowRun != null ? workflowRun.getRunId() : null);
        executionContext.setMemoryScope(buildMemoryScope(task, targetAgentType));
        executionContext.setSandboxPolicy(ExecutionSandboxPolicy.WORKSPACE_DEFAULT);
        executionContext.allowTool(ToolCapability.WORKSPACE_READ);

        if (targetAgentType == AgentType.CODER) {
            executionContext.allowTool(ToolCapability.WORKSPACE_WRITE);
        }
        if (targetAgentType == AgentType.TESTER) {
            executionContext.allowTool(ToolCapability.WORKSPACE_TEST);
        }

        return executionContext;
    }

    private String resolveWorkspaceRoot(Task task) {
        if (task.getConversationId() == null || task.getConversationId().isBlank()) {
            return null;
        }
        return workspaceManager.getConversationOrThrow(task.getConversationId()).getWorkspacePath();
    }

    private String buildMemoryScope(Task task, AgentType targetAgentType) {
        if (task.getConversationId() == null || task.getConversationId().isBlank()) {
            return null;
        }
        return "%s::%s::task-%s".formatted(
                task.getConversationId(),
                targetAgentType.name().toLowerCase(),
                task.getId()
        );
    }

    private <T extends Artifact> T findArtifact(AgentResult agentResult, Class<T> artifactClass) {
        if (agentResult == null || agentResult.getOutputArtifacts() == null) {
            return null;
        }
        for (Artifact artifact : agentResult.getOutputArtifacts()) {
            if (artifactClass.isInstance(artifact)) {
                return artifactClass.cast(artifact);
            }
        }
        return null;
    }

    private CompletableFuture<AgentResult> executeVerifierAsync(Agent verifierAgent, AgentTask agentTask) {
        return CompletableFuture.supplyAsync(() -> verifierAgent.execute(agentTask), verifierExecutor)
                .exceptionally(ex -> buildVerifierFailureResult(verifierAgent.getType(), agentTask, ex));
    }

    private AgentResult buildVerifierFailureResult(AgentType agentType,
                                                   AgentTask agentTask,
                                                   Throwable throwable) {
        Throwable cause = throwable instanceof CompletionException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        String message = cause == null ? "Unknown verifier failure" : cause.getMessage();
        AgentResult result = new AgentResult(
                agentTask.getTaskId(),
                agentTask.getPlanId(),
                agentTask.getRound(),
                agentType,
                false,
                "Verifier execution failed.",
                message,
                null,
                null,
                null
        );
        result.setIssues(List.of("Verifier failure: " + (message == null ? "unknown error" : message)));
        return result;
    }
}
