package org.web.fitness.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web.fitness.tool.CoachTool;

@Configuration
public class AgentRagConfig {
    @Bean
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore, CoachTool coachTool) {
            return ChatClient.builder(chatModel)
                    .defaultAdvisors(
                            QuestionAnswerAdvisor.builder(vectorStore)
                                    .searchRequest(SearchRequest.builder()
                                            .similarityThreshold(0.7)
                                            .topK(3)
                                            .build())
                                    .build()
                    )
                    .defaultTools(coachTool) // 注册@Tool工具
                    .build();
    }
}
