package com.qsh.multiagent.agent.tester.unit;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.report.model.UnitTestReport;

public class MockUnitTestAgent implements Agent {

    @Override
    public AgentType getType() {
        return AgentType.UNIT_TESTER;
    }

    @Override
    public AgentResult<?> execute(AgentTask task) {
        UnitTestReport report = new UnitTestReport(
                true,
                10,
                10,
                0,
                "All mock unit tests passed."
        );

        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                true,
                "Unit tests passed.",
                report,
                null
        );
    }
}
