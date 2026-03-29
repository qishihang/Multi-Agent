package com.qsh.multiagent.domain.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Plan {
    private String id;
    private String taskId;
    private Integer round;
    private String objective; // 目标
    private List<PlanStep> steps = new ArrayList<>();
    private String doneCriteria; // 完成标准

    public void addStep(PlanStep step) {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        this.steps.add(step);
    }

    public boolean hasSteps() {
        return steps != null && !steps.isEmpty();
    }
}
