package com.qsh.multiagent.infrastructure.llm.service;

import java.util.List;

public record PlannerPlanOutput(String objective,
                                String doneCriteria,
                                List<PlannerPlanStepOutput> steps) {
}
