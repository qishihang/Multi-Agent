package com.qsh.multiagent.infrastructure.tool.support;

import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.plan.PlanStep;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.infrastructure.tool.definition.AgentTool;
import com.qsh.multiagent.infrastructure.tool.executor.ToolExecutionResult;
import org.springframework.stereotype.Component;

//@Component
public class TaskPlanContextTool implements AgentTool {

    @Override
    public String getName() {
        return "taskPlanContextTool";
    }

    @Override
    public ToolExecutionResult execute(AgentTask task) {
        Task taskContext = task.getTask();
        Plan planContext = task.getPlan();

        StringBuilder stepBuilder = new StringBuilder();
        if (planContext != null && planContext.getSteps() != null) {
            for (PlanStep step : planContext.getSteps()) {
                stepBuilder.append("- stepNo: ").append(step.getStepNo()).append(", ")
                        .append("title: ").append(step.getTitle()).append(", ")
                        .append("description: ").append(step.getDescription()).append(", ")
                        .append("codingRequired: ").append(step.isCodingRequired()).append("\n");
            }
        }

        String content = """
                taskId: %s
                taskGoal: %s
                currentRound: %s
                planObjective: %s
                planDoneCriteria: %s
                steps:
                %s
                """.formatted(
                taskContext != null ? taskContext.getId() : "",
                taskContext != null ? taskContext.getGoal() : "",
                taskContext != null ? taskContext.getCurrentRound() : "",
                planContext != null ? planContext.getObjective() : "",
                planContext != null ? planContext.getDoneCriteria() : "",
                stepBuilder
        );

        return new ToolExecutionResult(getName(), true, content);
    }
}
