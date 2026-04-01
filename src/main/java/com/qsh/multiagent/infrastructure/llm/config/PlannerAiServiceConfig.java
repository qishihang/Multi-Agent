package com.qsh.multiagent.infrastructure.llm.config;

import com.qsh.multiagent.infrastructure.llm.service.PlannerAiService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlannerAiServiceConfig {

    @Bean
    public PlannerAiService plannerAiService(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                             @Qualifier("plannerChatMemoryProvider") ChatMemoryProvider plannerChatMemoryProvider,
                                             LangChain4jAiServiceFactory aiServiceFactory) {
        return aiServiceFactory.create(
                PlannerAiService.class,
                qwenChatModel,
                plannerChatMemoryProvider
        );
    }
}
