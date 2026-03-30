package com.qsh.multiagent.infrastructure.llm.service;

public record PlannerPlanStepOutput(Integer stepNo,
                                    String title,
                                    String description,
                                    Boolean codingRequired) {
}
