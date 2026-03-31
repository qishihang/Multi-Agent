package com.qsh.multiagent.agent.reviewer;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.report.model.ReviewReport;
import org.springframework.stereotype.Component;

//@Component
public class MockReviewerAgent implements Agent {

    @Override
    public AgentType getType() {
        return AgentType.REVIEWER;
    }

    @Override
    public AgentResult<?> execute(AgentTask task) {
        ReviewReport report = new ReviewReport(
                true,
                0,
                0,
                "Mock review passed."
        );
        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                true,
                "Reviewer passed.",
                report,
                null
        );
    }
}
