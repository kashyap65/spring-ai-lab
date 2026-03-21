package com.ailab.customer.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiConfig — Spring AI wiring.
 *
 * This class will grow each hour:
 *
 *   Hour 3 — ChatClient with system prompt
 *   Hour 4 — Add @Tool functions
 *   Hour 5 — Add VectorStore + EmbeddingModel
 *   Hour 6 — Add RAG QuestionAnswerAdvisor
 *   Hour 7 — Add ChatMemory
 *   Hour 8 — Add retry, observability, token budget
 *
 * For now: a minimal ChatClient bean so the app starts cleanly.
 */
@Configuration
public class AiConfig {

    /**
     * ChatClient.Builder is auto-configured by spring-ai-openai-spring-boot-starter.
     * We just customise it here with our default system prompt.
     *
     * Think of ChatClient like Spring's RestTemplate — it's the
     * primary entry point for all LLM interaction.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("""
                You are a helpful customer success assistant for a SaaS company.
                You have access to customer data and help the team understand
                customer health, identify churn risks, and find upsell opportunities.
                Always be concise, data-driven, and actionable in your responses.
                """)
            .build();
    }
}
