package com.qsh.multiagent.infrastructure.llm.config;

import com.qsh.multiagent.infrastructure.llm.service.CoderAiService;
import com.qsh.multiagent.infrastructure.tool.support.WorkspaceTools;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoderAiServiceConfig {

    @Bean
    public CoderAiService coderAiService(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                         WorkspaceTools workspaceTools,
                                         @Qualifier("coderChatMemoryProvider") ChatMemoryProvider coderChatMemoryProvider) {
        return AiServices.builder(CoderAiService.class)
                .chatModel(qwenChatModel)
                .chatMemoryProvider(coderChatMemoryProvider)
                .tools(workspaceTools)
                .build();
    }
}
