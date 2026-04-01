package com.qsh.multiagent.orchestration.workflow;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.domain.artifact.AggregateArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import com.qsh.multiagent.domain.workflow.WorkflowRun;
import com.qsh.multiagent.domain.workflow.WorkflowStage;
import com.qsh.multiagent.orchestration.aggregator.Aggregator;
import com.qsh.multiagent.orchestration.dispatcher.Dispatcher;
import com.qsh.multiagent.orchestration.planner.Planner;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class DefaultWorkflowEngine implements WorkflowEngine{
    private final Planner planner;
    private final Dispatcher dispatcher;
    private final Aggregator aggregator;

    @Override
    public Task run(Task task) {
        WorkflowRun workflowRun = initializeWorkflowRun(task);

        while (!isTaskFinished(task)) {
            syncTaskProgress(task, workflowRun);

            if (workflowRun.hasActiveRound() && !workflowRun.canContinue(task.getMaxRounds())) {
                workflowRun.markTerminated("Task terminated because max rounds were reached.");
                task.setStatus(TaskStatus.MAX_ROUND_REACHED);
                task.setFinalSummary("Task terminated because max rounds were reached.");
                syncTaskProgress(task, workflowRun);
                return task;
            }

            if (!workflowRun.hasActiveRound()) {
                workflowRun.startFirstRound();
                syncTaskProgress(task, workflowRun);
            }

            moveToStage(workflowRun, task, WorkflowStage.PLANNING);
            PlanArtifact planArtifact = planner.createPlanArtifact(task);

            moveToStage(workflowRun, task, WorkflowStage.CODING);
            AgentResult coderResult = dispatcher.dispatchToCoder(task, workflowRun, planArtifact);
            if (!coderResult.isSuccess()) {
                workflowRun.markFailed(coderResult.getErrorMessage());
                task.setStatus(TaskStatus.FAILED);
                task.setFinalSummary(coderResult.getErrorMessage());
                return task;
            }

            moveToStage(workflowRun, task, WorkflowStage.VERIFYING);
            List<AgentResult> verifyResults = dispatcher.dispatchToVerification(
                    task,
                    workflowRun,
                    planArtifact,
                    coderResult
            );

            moveToStage(workflowRun, task, WorkflowStage.AGGREGATING);
            AggregateArtifact aggregateArtifact = aggregator.aggregateArtifact(verifyResults);

            moveToStage(workflowRun, task, WorkflowStage.DECIDING);
            TaskDecision decision = planner.decide(task, workflowRun, aggregateArtifact);

            if (decision == TaskDecision.FINISH) {
                workflowRun.markCompleted();
                task.setStatus(TaskStatus.COMPLETED);
                task.setFinalSummary(aggregateArtifact.getSummary());
                syncTaskProgress(task, workflowRun);
                return task;
            }

            if (decision == TaskDecision.TERMINATE) {
                workflowRun.markTerminated(aggregateArtifact.getSummary());
                task.setStatus(TaskStatus.FAILED);
                task.setFinalSummary(aggregateArtifact.getSummary());
                syncTaskProgress(task, workflowRun);
                return task;
            }

            workflowRun.advanceRound();
            syncTaskProgress(task, workflowRun);
        }
        return task;
    }

    private WorkflowRun initializeWorkflowRun(Task task) {
        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setRunId(UUID.randomUUID().toString());
        workflowRun.setConversationId(task.getConversationId());
        workflowRun.setTaskId(task.getId());
        workflowRun.setCurrentRound(task.getCurrentRound());
        workflowRun.markRunning(WorkflowStage.CREATED);
        return workflowRun;
    }

    private void moveToStage(WorkflowRun workflowRun, Task task, WorkflowStage workflowStage) {
        workflowRun.moveTo(workflowStage);
        task.setStatus(mapTaskStatus(workflowStage));
    }

    private void syncTaskProgress(Task task, WorkflowRun workflowRun) {
        task.setCurrentRound(workflowRun.getCurrentRound());
    }

    private TaskStatus mapTaskStatus(WorkflowStage workflowStage) {
        return switch (workflowStage) {
            case PLANNING -> TaskStatus.PLANNING;
            case CODING -> TaskStatus.CODING;
            case VERIFYING -> TaskStatus.REVIEWING;
            case AGGREGATING, DECIDING -> TaskStatus.AGGREGATING;
            case COMPLETED -> TaskStatus.COMPLETED;
            case FAILED, TERMINATED -> TaskStatus.FAILED;
            case CREATED -> TaskStatus.CREATED;
        };
    }

    private boolean isTaskFinished(Task task) {
        return task.getStatus() == TaskStatus.COMPLETED
                || task.getStatus() == TaskStatus.FAILED
                || task.getStatus() == TaskStatus.MAX_ROUND_REACHED;
    }
}
