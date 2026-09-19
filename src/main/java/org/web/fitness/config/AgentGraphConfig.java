package org.web.fitness.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web.fitness.tool.CoachTool;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class AgentGraphConfig {
    private static final Logger log = LoggerFactory.getLogger(AgentGraphConfig.class);

    @Bean("agentChatClient")
    public ChatClient agentChatClient(ChatModel chatModel, VectorStore vectorStore){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.7)
                                        .topK(3)
                                        .build())
                                .build()
                )
                .build();
    }

    @Bean
    public AgentWorkflow agentWorkflow(VectorStore vectorStore, @Qualifier("agentChatClient") ChatClient  chatClient,
                                       CoachTool coachTool){
        return new AgentWorkflow(vectorStore,chatClient,coachTool);
    }

    public static class AgentWorkflow{
        private final VectorStore vectorStore;
        private final ChatClient chatClient;
        private final CoachTool coachTool;

        public AgentWorkflow(VectorStore vectorStore, ChatClient chatClient, CoachTool coachTool) {
            this.vectorStore = vectorStore;
            this.chatClient = chatClient;
            this.coachTool = coachTool;
        }

        public String execute(String query){
            // 1. 检索
            // 使用 Builder 模式正确构建 SearchRequest
            List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query)
                    .similarityThreshold(0.7)
                    .topK(3)
                    .build());
            System.out.println("检索到文档数量；"+documents.size());
            log.info("🔍 检索到 {} 篇相关文档", documents.size());
            // 2. 条件调用工具
            String toolResult = null;
            if(documents.isEmpty()){
                System.out.println("知识库无相关内容，自动调用健身工具");
                log.info("⚠️ 知识库无相关内容，自动调用健身工具");
                String coachName = extractName(query);
                toolResult = coachTool.getCoach(coachName);
            }
            // 3. 生成答案
            String context = documents.stream().map(Document::getFormattedContent)
                    .collect(Collectors.joining("\n"));
            //提示词生成
            String prompt = buildPrompt(query, context, toolResult);
            return chatClient.prompt().user(prompt).call().content();
        }

        private String extractName(String query){
            if(query.contains("健身")){
                return "健身教练";
            }else if(query.contains("营养")){
                return "营养师";
            }else if(query.contains("减脂")){
                return "减脂师";
            }else {
                return "未知";
            }
        }

        private String buildPrompt(String query, String context, String toolResult){
            StringBuilder sb = new StringBuilder();
            sb.append("请基于以下信息回答用户问题。\n");
            if(!context.isEmpty()){
                sb.append("知识库相关信息：\n").append(context).append("\n");
            }
            if(toolResult!=null){
                sb.append("外部工具查询结果：").append(toolResult).append("\n");
            }
            sb.append("用户问题：").append(query);
            sb.append("\n请给出简洁专业的回答。");
            return sb.toString();
        }
    }
}
