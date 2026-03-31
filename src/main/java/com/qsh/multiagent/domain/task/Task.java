package com.qsh.multiagent.domain.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private String id;
    private String goal;
    private TaskStatus status;
    private Integer currentRound;
    private Integer maxRounds;
    private String currentPlanId;
    private String finalSummary;
    private String conversationId;
    private String workspacePath;

    public boolean canContinue(){
        return currentRound != null && maxRounds != null && currentRound < maxRounds;
    }

    public void nextRound(){
        if(currentRound == null){
            currentRound = 1;
            return;
        }
        currentRound++;
    }

    public boolean isFinished(){
        return status == TaskStatus.COMPLETED || status == TaskStatus.FAILED || status == TaskStatus.MAX_ROUND_REACHED;
    }
}
