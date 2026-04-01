package com.qsh.multiagent.infrastructure.llm.config;

import com.qsh.multiagent.infrastructure.llm.service.ReviewerAiService;
import com.qsh.multiagent.infrastructure.tool.support.WorkspaceTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewerAiServiceConfig {

    @Bean
    public ReviewerAiService reviewerAiService(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                               WorkspaceTools workspaceTools,
                                               @Qualifier("reviewerChatMemoryProvider") ChatMemoryProvider reviewerChatMemoryProvider,
                                               LangChain4jAiServiceFactory aiServiceFactory) {
        return aiServiceFactory.create(
                ReviewerAiService.class,
                qwenChatModel,
                reviewerChatMemoryProvider,
                workspaceTools
        );
    }
}
