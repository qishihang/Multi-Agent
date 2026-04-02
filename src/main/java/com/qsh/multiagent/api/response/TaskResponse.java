package com.qsh.multiagent.api.response;

import com.qsh.multiagent.domain.task.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse { // 单独定义Reponse，不能把Task暴露给api层

    private String taskId;
    private String goal;
    private TaskStatus status;
    private Integer currentRound;
    private Integer maxRounds;
    private String finalSummary;
}
