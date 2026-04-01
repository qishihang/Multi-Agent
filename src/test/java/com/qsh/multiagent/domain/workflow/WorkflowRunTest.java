package com.qsh.multiagent.domain.workflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WorkflowRunTest {

    @Test
    void should_start_and_advance_rounds_from_run_state() {
        WorkflowRun workflowRun = new WorkflowRun();

        Assertions.assertFalse(workflowRun.hasActiveRound());

        workflowRun.startFirstRound();
        Assertions.assertTrue(workflowRun.hasActiveRound());
        Assertions.assertEquals(1, workflowRun.getCurrentRound());

        workflowRun.advanceRound();
        Assertions.assertEquals(2, workflowRun.getCurrentRound());
    }

    @Test
    void should_evaluate_round_continuation_against_max_rounds() {
        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setCurrentRound(1);

        Assertions.assertTrue(workflowRun.canContinue(3));
        Assertions.assertFalse(workflowRun.canContinue(1));
        Assertions.assertFalse(workflowRun.canContinue(null));
    }

    @Test
    void should_track_stage_and_terminal_status() {
        WorkflowRun workflowRun = new WorkflowRun();

        workflowRun.markRunning(WorkflowStage.CREATED);
        Assertions.assertEquals(WorkflowRunStatus.RUNNING, workflowRun.getStatus());
        Assertions.assertEquals(WorkflowStage.CREATED, workflowRun.getCurrentStage());
        Assertions.assertNotNull(workflowRun.getStartedAt());

        workflowRun.moveTo(WorkflowStage.CODING);
        Assertions.assertEquals(WorkflowStage.CODING, workflowRun.getCurrentStage());

        workflowRun.markCompleted();
        Assertions.assertEquals(WorkflowRunStatus.COMPLETED, workflowRun.getStatus());
        Assertions.assertEquals(WorkflowStage.COMPLETED, workflowRun.getCurrentStage());
        Assertions.assertNotNull(workflowRun.getFinishedAt());
    }
}
