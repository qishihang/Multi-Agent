package com.qsh.multiagent.orchestration;

import com.qsh.multiagent.agent.coder.MockCoderAgent;
import com.qsh.multiagent.agent.reviewer.MockReviewerAgent;
import com.qsh.multiagent.agent.tester.build.MockBuildAgent;
import com.qsh.multiagent.agent.tester.lint.MockLintAgent;
import com.qsh.multiagent.agent.tester.unit.MockUnitTestAgent;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import com.qsh.multiagent.orchestration.aggregator.SimpleAggregator;
import com.qsh.multiagent.orchestration.dispatcher.LocalDispatcher;
import com.qsh.multiagent.orchestration.planner.MockPlanner;
import com.qsh.multiagent.orchestration.workflow.DefaultWorkflowEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultWorkflowEngineTest {

    @Test
    void should_complete_task_when_all_mock_agents_pass(){
        MockPlanner planner = new MockPlanner();
        SimpleAggregator aggregator = new SimpleAggregator();

        MockCoderAgent coderAgent = new MockCoderAgent();
        MockReviewerAgent reviewerAgent = new MockReviewerAgent();
        MockBuildAgent buildAgent = new MockBuildAgent();
        MockUnitTestAgent unitTestAgent = new MockUnitTestAgent();
        MockLintAgent lintAgent = new MockLintAgent();

        LocalDispatcher dispatcher = new LocalDispatcher(
                coderAgent,
                reviewerAgent,
                buildAgent,
                unitTestAgent,
                lintAgent
        );

        DefaultWorkflowEngine workflowEngine = new DefaultWorkflowEngine(
                planner,
                dispatcher,
                aggregator
        );

        Task task = new Task();
        task.setId("task-001");
        task.setGoal("Create a mock multi-agent workflow");
        task.setStatus(TaskStatus.CREATED);
        task.setCurrentRound(null);
        task.setMaxRounds(3);

        Task result = workflowEngine.run(task);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(TaskStatus.COMPLETED, result.getStatus());
        Assertions.assertNotNull(result.getCurrentPlanId());
        Assertions.assertNotNull(result.getFinalSummary());
        Assertions.assertEquals(1, result.getCurrentRound());
    }
}
