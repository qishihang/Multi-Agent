package com.qsh.multiagent.domain.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowRun {

    private String runId;
    private String conversationId;
    private String taskId;
    private Integer currentRound;
    private WorkflowStage currentStage;
    private WorkflowRunStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private String failureReason;

    public void markRunning(WorkflowStage stage) {
        this.status = WorkflowRunStatus.RUNNING;
        this.currentStage = stage;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void moveTo(WorkflowStage stage) {
        this.currentStage = stage;
        if (this.status == null || this.status == WorkflowRunStatus.CREATED) {
            this.status = WorkflowRunStatus.RUNNING;
        }
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void markCompleted() {
        this.status = WorkflowRunStatus.COMPLETED;
        this.currentStage = WorkflowStage.COMPLETED;
        this.finishedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = WorkflowRunStatus.FAILED;
        this.currentStage = WorkflowStage.FAILED;
        this.failureReason = reason;
        this.finishedAt = Instant.now();
    }

    public void markTerminated(String reason) {
        this.status = WorkflowRunStatus.TERMINATED;
        this.currentStage = WorkflowStage.TERMINATED;
        this.failureReason = reason;
        this.finishedAt = Instant.now();
    }

    public boolean hasActiveRound() {
        return currentRound != null;
    }

    public void startFirstRound() {
        if (currentRound == null) {
            currentRound = 1;
        }
    }

    public void advanceRound() {
        if (currentRound == null) {
            currentRound = 1;
            return;
        }
        currentRound++;
    }

    public boolean canContinue(Integer maxRounds) {
        return currentRound != null && maxRounds != null && currentRound < maxRounds;
    }
}
