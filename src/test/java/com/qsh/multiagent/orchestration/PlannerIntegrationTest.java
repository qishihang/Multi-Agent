package com.qsh.multiagent.orchestration;

import com.qsh.multiagent.domain.plan.Plan;
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
        task.setCurrentRound(1);
        task.setMaxRounds(3);

        Plan plan = planner.createPlan(task);

        Assertions.assertNotNull(plan);
        Assertions.assertNotNull(plan.getObjective());
        Assertions.assertNotNull(plan.getDoneCriteria());
        Assertions.assertTrue(plan.hasSteps());
    }
}
