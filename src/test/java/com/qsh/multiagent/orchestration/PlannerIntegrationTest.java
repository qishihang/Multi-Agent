package com.qsh.multiagent.orchestration;

import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.orchestration.planner.Planner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PlannerIntegrationTest {

    @Autowired
    private Planner planner;

    @Test
    void should_create_plan_from_real_planner() {
        Task task = new Task();
        task.setId("task-real-planner");
        task.setGoal("Create a login feature plan");
        task.setConversationId("conv-planner-001");
        task.setCurrentRound(1);
        task.setMaxRounds(3);

        PlanArtifact plan = planner.createPlanArtifact(task);

        Assertions.assertNotNull(plan);
        Assertions.assertNotNull(plan.getObjective());
        Assertions.assertNotNull(plan.getDoneCriteria());
        Assertions.assertNotNull(plan.getSteps());
        Assertions.assertFalse(plan.getSteps().isEmpty());
    }

    @Test
    void should_create_plan_with_task_memory() {
        Task task = new Task();
        task.setId("task-memory-001");
        task.setGoal("Create a login module plan");
        task.setConversationId("conv-planner-002");
        task.setCurrentRound(1);
        task.setMaxRounds(3);

        PlanArtifact firstPlan = planner.createPlanArtifact(task);
        PlanArtifact secondPlan = planner.createPlanArtifact(task);

        Assertions.assertNotNull(firstPlan);
        Assertions.assertNotNull(secondPlan);
        Assertions.assertNotNull(firstPlan.getSteps());
        Assertions.assertNotNull(secondPlan.getSteps());
        Assertions.assertFalse(firstPlan.getSteps().isEmpty());
        Assertions.assertFalse(secondPlan.getSteps().isEmpty());
    }
}
