package com.qsh.multiagent.infrastructure.llm.config;

import com.qsh.multiagent.infrastructure.llm.service.CoderAiService;
import com.qsh.multiagent.infrastructure.tool.support.WorkspaceTools;
import com.qsh.multiagent.infrastructure.tool.support.WorkspaceWriteTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoderAiServiceConfig {

    @Bean
    public CoderAiService coderAiService(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                         WorkspaceTools workspaceTools,
                                         WorkspaceWriteTools workspaceWriteTools,
                                         @Qualifier("coderChatMemoryProvider") ChatMemoryProvider coderChatMemoryProvider,
                                         LangChain4jAiServiceFactory aiServiceFactory) {
        return aiServiceFactory.create(
                CoderAiService.class,
                qwenChatModel,
                coderChatMemoryProvider,
                workspaceTools,
                workspaceWriteTools
        );
    }
}
