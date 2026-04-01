package com.qsh.multiagent.infrastructure.llm.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


public interface ReviewerAiService {

    @SystemMessage("""
            你是系统中的审查智能体。
            你必须严格遵守用户消息中的技能说明、输入上下文和输出契约。
            你必须返回结构化结果，不要输出多余解释。
            """)
    ReviewerAnalysisOutput review(@UserMessage String userMessage);
}
