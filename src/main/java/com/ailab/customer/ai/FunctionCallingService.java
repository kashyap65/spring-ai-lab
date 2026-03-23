package com.ailab.customer.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * FunctionCallingService — Hour 4
 *
 * Wires CustomerAiTools into ChatClient.
 * The model reads tool descriptions and decides which
 * to call — you never hardcode which tool runs when.
 *
 * This is the key difference from Hour 3:
 *   Hour 3: YOU decide what data to fetch, inject it manually
 *   Hour 4: THE MODEL decides what data to fetch, calls tools
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FunctionCallingService {

    private final ChatClient chatClient;
    private final CustomerAiTools customerAiTools;

    /**
     * Single entry point — natural language question,
     * model decides which tools to call, returns answer.
     *
     * The user can ask ANYTHING — the model figures out
     * which combination of tools to call.
     */
    public String ask(String question) {
        log.info("Function calling ask: {}", question);

        return chatClient.prompt()
                .system("""
                You are a customer success AI assistant for a SaaS company.
                
                You have access to tools that query our customer database.
                Always use the appropriate tools to get real data before answering.
                Never invent or estimate customer data — always fetch it.
                
                When answering:
                - Be specific with numbers from the tool results
                - Prioritise actionable insights
                - Suggest concrete next steps
                - If multiple tools are needed, call them all
                """)
                .user(question)
                // Register ALL tools — model picks which ones to call
                .tools(customerAiTools)
                .call()
                .content();
    }

    /**
     * Churn-focused analysis — system prompt steers
     * the model to focus on churn signals specifically.
     */
    public String analyseChurnRisk(int thresholdDays) {
        return chatClient.prompt()
                .system("""
                You are a churn risk analyst.
                Use the available tools to identify at-risk customers.
                Structure your response as:
                1. Summary (total at-risk count and revenue impact)
                2. Critical cases (needs immediate action)
                3. Moderate cases (needs follow-up this week)
                4. Recommended actions (prioritised list)
                """)
                .user("Analyse churn risk for customers inactive for more than "
                        + thresholdDays + " days. Include revenue impact.")
                .tools(customerAiTools)
                .call()
                .content();
    }

    /**
     * Revenue growth analysis — model uses multiple tools
     * to cross-reference upsell candidates with top customers.
     */
    public String analyseRevenueGrowth() {
        return chatClient.prompt()
                .system("""
                You are a revenue growth analyst.
                Use the available tools to identify growth opportunities.
                Combine upsell candidates with current top customer data.
                Return a prioritised list with expected revenue impact.
                """)
                .user("""
                Analyse our revenue growth opportunities:
                1. Find BASIC customers ready to upgrade (min 40 logins)
                2. Find our top 5 customers for context on what good looks like
                3. Cross-reference to identify best upsell opportunities
                4. Estimate monthly revenue impact if top 3 candidates upgrade
                """)
                .tools(customerAiTools)
                .call()
                .content();
    }

    /**
     * Support health analysis — model combines ticket data
     * with customer value to prioritise support queue.
     */
    public String analyseSupportHealth() {
        return chatClient.prompt()
                .system("""
                You are a customer support analyst.
                Prioritise support based on customer value and urgency.
                Always mention the customer's plan and lifetime value
                so support team knows who to handle first.
                """)
                .user("""
                Analyse our support queue:
                1. Find all customers with open tickets
                2. Get overall stats to understand the scale
                3. Prioritise the queue by customer value + ticket count
                4. Flag any PREMIUM or ENTERPRISE customers needing immediate attention
                """)
                .tools(customerAiTools)
                .call()
                .content();
    }
}