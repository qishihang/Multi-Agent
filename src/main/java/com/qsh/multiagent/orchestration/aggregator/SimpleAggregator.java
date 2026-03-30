package com.qsh.multiagent.orchestration.aggregator;

import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.domain.report.model.AggregatedResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SimpleAggregator implements Aggregator{

    @Override
    public AggregatedResult aggregate(List<AgentResult<?>> results){
        AggregatedResult aggregatedResult = new AggregatedResult();

        if(results == null || results.isEmpty()){
            aggregatedResult.setAllPassed(false);
            aggregatedResult.setHasBlockingIssues(true);
            aggregatedResult.setSummary("No verification results were produced.");
            aggregatedResult.setSuggestion("Check dispatcher and test agents.");
            return aggregatedResult;
        }

        AgentResult<?> firstResult = results.get(0);
        aggregatedResult.setTaskId(firstResult.getTaskId());
        aggregatedResult.setPlanId(firstResult.getPlanId());
        aggregatedResult.setRound(firstResult.getRound());

        boolean allPassed = true;
        boolean hasBlockingIssues = false;
        StringBuilder summaryBuilder = new StringBuilder();

        for(AgentResult<?> result : results){
            if (!result.isSuccess()){
                allPassed = false;
                hasBlockingIssues = true;
            }
            summaryBuilder
                    .append(result.getAgentType())
                    .append(": ")
                    .append(result.getSummary())
                    .append("; ");

            if(allPassed){
                aggregatedResult.setSuggestion("Finish current task.");
            }else{
                aggregatedResult.setSuggestion("Continue to next round and fix issues.");
            }
        }

        aggregatedResult.setAllPassed(allPassed);
        aggregatedResult.setHasBlockingIssues(hasBlockingIssues);
        aggregatedResult.setSummary(summaryBuilder.toString());

        return aggregatedResult;
    }
}
