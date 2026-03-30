package com.qsh.multiagent.agent.tester.lint;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.report.model.LintReport;

public class MockLintAgent implements Agent {

    @Override
    public AgentType getType() {
        return AgentType.LINT_TESTER;
    }

    @Override
    public AgentResult<?> execute(AgentTask task) {
        LintReport report = new LintReport(
                true,
                0,
                0,
                "Mock lint passed."
        );

        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                true,
                "Lint passed.",
                report,
                null
        );
    }
}
