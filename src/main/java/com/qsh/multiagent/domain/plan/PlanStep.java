package com.qsh.multiagent.domain.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanStep {
    private Integer stepNo;
    private String title;
    private String description;
    private boolean codingRequired;
}
