package com.ailab.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring AI Lab — Customer Microservice
 *
 * 8-hour hands-on workspace for learning:
 *   Hour 1  — Setup (this file!)
 *   Hour 2  — Prompt Engineering
 *   Hour 3  — Spring AI ChatClient
 *   Hour 4  — Structured Output & Function Calling
 *   Hour 5  — RAG: Embeddings & Vector Store
 *   Hour 6  — RAG Pipeline over SQL data
 *   Hour 7  — LangChain4j: Chains, Memory, Agents
 *   Hour 8  — Production hardening
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringAiLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiLabApplication.class, args);
    }
}
