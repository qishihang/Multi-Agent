package com.qsh.multiagent.orchestration.workflow;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.domain.plan.Plan;
import com.qsh.multiagent.domain.report.model.AggregatedResult;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import com.qsh.multiagent.domain.workflow.TaskDecision;
import com.qsh.multiagent.orchestration.aggregator.Aggregator;
import com.qsh.multiagent.orchestration.dispatcher.Dispatcher;
import com.qsh.multiagent.orchestration.planner.Planner;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DefaultWorkflowEngine implements WorkflowEngine{
    private final Planner planner;
    private final Dispatcher dispatcher;
    private final Aggregator aggregator;

    @Override
    public Task run(Task task) {
        while(!task.isFinished()){
            if(!task.canContinue() && task.getCurrentRound() != null){
                task.setStatus(TaskStatus.MAX_ROUND_REACHED);
                task.setFinalSummary("Task terminated because max rounds were reached.");
                return task;
            }

            if(task.getCurrentRound() == null){
                task.nextRound();
            }

            task.setStatus(TaskStatus.PLANNING);
            Plan plan = planner.createPlan(task);
            task.setCurrentPlanId(plan.getId());

            task.setStatus(TaskStatus.CODING);
            AgentResult<?> coderResult = dispatcher.dispatchToCoder(task, plan);
            if (!coderResult.isSuccess()) {
                task.setStatus(TaskStatus.FAILED);
                task.setFinalSummary(coderResult.getErrorMessage());
                return task;
            }

            task.setStatus(TaskStatus.REVIEWING);
            List<AgentResult<?>> verifyResults = dispatcher.dispatchToReviewAndTest(task, plan);

            task.setStatus(TaskStatus.AGGREGATING);
            AggregatedResult aggregatedResult = aggregator.aggregate(verifyResults);

            TaskDecision decision = planner.decide(task, aggregatedResult);

            if (decision == TaskDecision.FINISH) {
                task.setStatus(TaskStatus.COMPLETED);
                task.setFinalSummary(aggregatedResult.getSummary());
                return task;
            }

            if (decision == TaskDecision.TERMINATE) {
                task.setStatus(TaskStatus.FAILED);
                task.setFinalSummary(aggregatedResult.getSummary());
                return task;
            }

            task.nextRound();
        }
        return  task;
    }
}
