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
    private String finalSummary;
    private String conversationId;
}
