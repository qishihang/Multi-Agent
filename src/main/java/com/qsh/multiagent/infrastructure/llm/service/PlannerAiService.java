package com.qsh.multiagent.infrastructure.llm.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
           chatModel = "qwenChatModel",
           chatMemoryProvider = "plannerChatMemoryProvider")
public interface PlannerAiService {

    @SystemMessage("""
            You are a planning agent in a multi-agent software engineering system.
            Your task is to create a concise execution plan for the current software task.

            Return a structured plan with:
            - objective
            - doneCriteria
            - steps

            Rules:
            - Keep the plan small and actionable
            - Prefer 1 to 3 steps
            - Each step must be concrete
            - Mark codingRequired as true only when code changes are needed
            """)
    PlannerPlanOutput createPlan(@MemoryId String taskId, @UserMessage String userMessage);
}
