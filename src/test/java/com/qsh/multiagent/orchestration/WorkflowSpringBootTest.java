package com.qsh.multiagent.orchestration;

import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import com.qsh.multiagent.orchestration.workflow.WorkflowEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WorkflowSpringBootTest {

    @Autowired
    WorkflowEngine workflowEngine;

    @Test
    void should_run_workflow_in_spring_context() {
        Task task = new Task();
        task.setId("task-002");
        task.setGoal("Run workflow inside Spring context");
        task.setStatus(TaskStatus.CREATED);
        task.setCurrentRound(null);
        task.setMaxRounds(3);

        Task result = workflowEngine.run(task);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(TaskStatus.COMPLETED, result.getStatus());
        Assertions.assertNotNull(result.getCurrentPlanId());
        Assertions.assertNotNull(result.getFinalSummary());
    }
}
