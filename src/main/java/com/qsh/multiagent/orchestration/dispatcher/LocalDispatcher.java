package com.qsh.multiagent.orchestration.dispatcher;

import com.qsh.multiagent.agent.coder.MockCoderAgent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.agent.reviewer.DefaultReviewerAgent;
import com.qsh.multiagent.agent.reviewer.MockReviewerAgent;
import com.qsh.multiagent.agent.tester.build.MockBuildAgent;
import com.qsh.multiagent.agent.tester.lint.MockLintAgent;
import com.qsh.multiagent.agent.tester.unit.MockUnitTestAgent;
import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.task.Task;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Component
public class LocalDispatcher implements Dispatcher{

    private final MockCoderAgent coderAgent;
    private final DefaultReviewerAgent reviewerAgent;
    private final MockBuildAgent buildAgent;
    private final MockUnitTestAgent unitTestAgent;
    private final MockLintAgent lintAgent;

    @Override
    public AgentResult<?> dispatchToCoder(Task task, Plan plan){
        AgentTask agentTask = new AgentTask(
                task.getId(),
                plan.getId(),
                plan.getRound(),
                AgentType.CODER,
                plan.getObjective(),
                "Mock coding input",
                task,
                plan
        );
        return coderAgent.execute(agentTask);
    }

    @Override
    public List<AgentResult<?>> dispatchToReviewAndTest(Task task, Plan plan){
        List<AgentResult<?>> results = new ArrayList<>();

        AgentTask reviewerTask = new AgentTask(
                task.getId(),
                plan.getId(),
                plan.getRound(),
                AgentType.REVIEWER,
                plan.getObjective(),
                "Mock review input",
                task,
                plan
        );

        AgentTask buildTask = new AgentTask(
                task.getId(),
                plan.getId(),
                plan.getRound(),
                AgentType.BUILD_TESTER,
                plan.getObjective(),
                "Mock build input",
                task,
                plan
        );

        AgentTask unitTask = new AgentTask(
                task.getId(),
                plan.getId(),
                plan.getRound(),
                AgentType.UNIT_TESTER,
                plan.getObjective(),
                "Mock unit test input",
                task,
                plan
        );

        AgentTask lintTask = new AgentTask(
                task.getId(),
                plan.getId(),
                plan.getRound(),
                AgentType.LINT_TESTER,
                plan.getObjective(),
                "Mock lint input",
                task,
                plan
        );

        results.add(reviewerAgent.execute(reviewerTask));
        results.add(buildAgent.execute(buildTask));
        results.add(unitTestAgent.execute(unitTask));
        results.add(lintAgent.execute(lintTask));

        return results;
    }
}
