package com.qsh.multiagent.agent.tester.build;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.report.model.BuildReport;

public class MockBuildAgent implements Agent {

    @Override
    public AgentType getType() {
        return AgentType.BUILD_TESTER;
    }

    @Override
    public AgentResult<?> execute(AgentTask task) {
        BuildReport report = new BuildReport(
                true,
                "Mock build succeeded.",
                null
        );

        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                true,
                "Build passed.",
                report,
                null
        );
    }
}
