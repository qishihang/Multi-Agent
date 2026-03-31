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
            你是系统中的规划智能体。
            你必须严格遵守用户消息中的技能说明、输入上下文和输出契约。
            你必须返回结构化结果，不要输出多余解释。
            """)
    PlannerPlanOutput createPlan(@MemoryId String conversationId, @UserMessage String userMessage);
}
