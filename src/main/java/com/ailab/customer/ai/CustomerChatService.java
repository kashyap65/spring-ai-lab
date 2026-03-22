package com.ailab.customer.ai;

import com.ailab.customer.ai.dto.ChurnRiskResult;
import com.ailab.customer.ai.dto.CustomerSummary;
import com.ailab.customer.ai.dto.UpsellOpportunity;
import com.ailab.customer.model.Customer;
import com.ailab.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * CustomerChatService — Hour 3
 * <p>
 * Demonstrates all 4 ChatClient response modes:
 * 1. .content()      → String
 * 2. .entity()       → Java record (structured output)
 * 3. .stream()       → Flux<String> (streaming)
 * 4. .chatResponse() → full metadata
 * <p>
 * Notice: CustomerService is injected here.
 * The AI layer calls YOUR existing business logic.
 * No changes needed to CustomerService — ever.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerChatService {

    private final ChatClient chatClient;
    private final CustomerService customerService;

    // ─────────────────────────────────────────────────────────
    // MODE 1 — .content() → String
    // Plain text response. Good for summaries, emails, Q&A.
    // ─────────────────────────────────────────────────────────

    /**
     * Natural language Q&A about a specific customer.
     * The model answers ONLY from injected data — no hallucination.
     */
    public String askAboutCustomer(Customer customer, String question) {
        log.debug("AI Q&A for customer: {} | question: {}",
                customer.getEmail(), question);

        String context = buildCustomerContext(customer);

        return chatClient.prompt()
                .system("""
                        You are a customer success analyst.
                        Answer questions using ONLY the customer data provided.
                        If the answer is not in the data, say exactly:
                        "That information is not available in the customer record."
                        Be concise — maximum 3 sentences.
                        """)
                .user(context + "\n\nQUESTION: " + question)
                .call()
                .content();
    }

    /**
     * Conversational chat — user can ask anything about
     * the entire customer base in natural language.
     */
    public String chat(String userMessage) {
        // Get live stats to inject as context
        CustomerService.CustomerStats stats = customerService.getStats();

        String context = """
                CUSTOMER BASE SUMMARY:
                Total customers: %d
                Active: %d | Churned: %d
                FREE: %d | BASIC: %d | PREMIUM: %d | ENTERPRISE: %d
                Monthly revenue: $%.2f
                """.formatted(
                stats.totalCustomers(),
                stats.activeCount(), stats.churnedCount(),
                stats.freeCount(), stats.basicCount(),
                stats.premiumCount(), stats.enterpriseCount(),
                stats.monthlyRevenue()
        );

        return chatClient.prompt()
                .system("""
                        You are a customer success AI assistant for a SaaS company.
                        Use the customer base data provided to answer questions.
                        Be data-driven, concise, and actionable.
                        Always suggest a next step.
                        """)
                .user(context + "\n\nQUESTION: " + userMessage)
                .call()
                .content();
    }

    // ─────────────────────────────────────────────────────────
    // MODE 2 — .entity() → Java record
    // Structured output — Spring AI parses JSON → your record.
    // Java analogy: RestTemplate.getForObject(url, MyClass.class)
    // ─────────────────────────────────────────────────────────

    /**
     * Returns a typed CustomerSummary record — not a raw String.
     * Spring AI appends JSON schema instructions to your prompt
     * automatically, then deserialises the response.
     */
    public CustomerSummary getStructuredSummary(Customer customer) {
        log.debug("Structured summary for: {}", customer.getEmail());

        long daysSinceLogin = getDaysSinceLogin(customer);

        String prompt = """
                Analyse this customer and return a structured summary.
                            
                Customer data:
                Name: %s %s
                Plan: %s | Status: %s
                Days since last login: %d
                Login count: %d
                Monthly spend: $%.2f | Lifetime value: $%.2f
                Open tickets: %d
                Notes: %s
                """.formatted(
                customer.getFirstName(), customer.getLastName(),
                customer.getPlan(), customer.getStatus(),
                daysSinceLogin,
                customer.getLoginCount(),
                customer.getMonthlySpend(),
                customer.getLifetimeValue(),
                customer.getOpenTickets(),
                customer.getNotes()
        );

        // .entity() — Spring AI automatically:
        // 1. Appends JSON schema to your prompt
        // 2. Calls GPT-4o
        // 3. Parses response JSON → CustomerSummary record
        // You never write ObjectMapper.readValue() manually
        return chatClient.prompt()
                .system("""
                        You are a customer analyst.
                        Analyse the customer and return structured data.
                        healthStatus must be one of: HEALTHY, AT_RISK, CRITICAL
                        """)
                .user(prompt)
                .call()
                .entity(CustomerSummary.class);
    }

    /**
     * Churn risk as a typed record.
     * Result feeds directly into your business logic —
     * no JSON parsing, no null checks on string fields.
     */
    public ChurnRiskResult assessChurnRisk(Customer customer) {
        long daysSinceLogin = getDaysSinceLogin(customer);

        String prompt = """
                Assess churn risk for this customer:
                            
                Plan: %s | Status: %s
                Days since last login: %d
                Login count (total): %d
                Open support tickets: %d
                Monthly spend: $%.2f
                Notes: %s
                            
                Rules for riskLevel:
                LOW      = active, engaged, no issues
                MEDIUM   = some inactivity or tickets
                HIGH     = significant inactivity (>30 days) or multiple tickets
                CRITICAL = extreme inactivity (>60 days) + issues
                            
                daysUntilChurnEstimate: your best estimate based on patterns.
                """.formatted(
                customer.getPlan(), customer.getStatus(),
                daysSinceLogin,
                customer.getLoginCount(),
                customer.getOpenTickets(),
                customer.getMonthlySpend(),
                customer.getNotes()
        );

        return chatClient.prompt()
                .system("You are a churn risk analyst. Be precise and data-driven.")
                .user(prompt)
                .call()
                .entity(ChurnRiskResult.class);
    }

    /**
     * Upsell analysis as a typed record.
     * The UpsellOpportunity object can be directly used by
     * your CRM integration or email automation system.
     */
    public UpsellOpportunity analyseUpsellOpportunity(Customer customer) {
        long daysSinceLogin = getDaysSinceLogin(customer);

        String prompt = """
                Analyse upsell opportunity for this customer:
                            
                Name: %s | Current plan: %s
                Login count: %d | Days since login: %d
                Monthly spend: $%.2f | Lifetime value: $%.2f
                Open tickets: %d
                Notes: %s
                            
                Plans in order: FREE → BASIC → PREMIUM → ENTERPRISE
                Only suggest upgrading one tier at a time.
                Set shouldUpsell=false if customer is already on ENTERPRISE
                or shows churn signals.
                """.formatted(
                customer.getFirstName(), customer.getPlan(),
                customer.getLoginCount(), daysSinceLogin,
                customer.getMonthlySpend(), customer.getLifetimeValue(),
                customer.getOpenTickets(), customer.getNotes()
        );

        return chatClient.prompt()
                .system("You are a revenue growth analyst. Focus on customer fit, not just revenue.")
                .user(prompt)
                .call()
                .entity(UpsellOpportunity.class);
    }

    // ─────────────────────────────────────────────────────────
    // MODE 3 — .stream() → Flux<String>
    // Tokens stream back one by one — like ChatGPT typing effect.
    // Java analogy: InputStream vs reading whole file at once.
    // Use for: long responses, chat UIs, real-time feel.
    // ─────────────────────────────────────────────────────────

    /**
     * Streaming chat — tokens arrive one by one via Server-Sent Events.
     * The controller converts this Flux into an SSE stream.
     * Browser receives tokens in real time as GPT-4o generates them.
     */
    public Flux<String> streamChat(String userMessage) {
        CustomerService.CustomerStats stats = customerService.getStats();

        return chatClient.prompt()
                .system("""
                        You are a customer success AI assistant.
                        Give thorough, detailed answers — streaming is used for longer responses.
                        Structure your response with clear sections.
                        """)
                .user("Customer base context: " + stats.totalCustomers()
                        + " customers, $" + stats.monthlyRevenue() + " MRR\n\n"
                        + "Question: " + userMessage)
                .stream()      // ← key difference: stream() not call()
                .content();    // returns Flux<String> — one token per emission
    }

    /**
     * Streaming customer analysis — detailed report streamed token by token.
     */
    public Flux<String> streamCustomerAnalysis(Customer customer) {
        return chatClient.prompt()
                .system("""
                        You are a senior customer success manager writing a detailed report.
                        Structure your analysis with these sections:
                        1. Executive Summary
                        2. Engagement Analysis
                        3. Risk Assessment
                        4. Revenue Potential
                        5. Recommended Actions (prioritised list)
                        Be specific and data-driven.
                        """)
                .user(buildCustomerContext(customer))
                .stream()
                .content();
    }

    // ─────────────────────────────────────────────────────────
    // MODE 4 — .chatResponse() → full metadata
    // Full response object with token counts, model info.
    // Use for: cost tracking, logging, observability (Hour 8).
    // ─────────────────────────────────────────────────────────

    /**
     * Returns response WITH token usage metadata.
     * Critical for cost tracking in production.
     * At GPT-4o pricing: input ~$2.50/1M tokens, output ~$10/1M tokens
     */
    public Map<String, Object> chatWithMetadata(String question) {
        CustomerService.CustomerStats stats = customerService.getStats();

        ChatResponse response = chatClient.prompt()
                .system("You are a customer success assistant.")
                .user("Stats: " + stats + "\n\nQuestion: " + question)
                .call()
                .chatResponse();  // ← full response object

        // Extract text content
        assert response != null;
        String content = response.getResult()
                .getOutput().getText();

        // Extract token usage — critical for cost monitoring
        Usage usage = response.getMetadata().getUsage();

        log.info("Token usage — input: {}, output: {}, total: {}",
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());

        // Estimated cost calculation
        double inputCost = usage.getPromptTokens() / 1_000_000.0 * 2.50;
        double outputCost = usage.getCompletionTokens() / 1_000_000.0 * 10.00;
        double totalCost = inputCost + outputCost;

        return Map.of(
                "answer", content,
                "inputTokens", usage.getPromptTokens(),
                "outputTokens", usage.getCompletionTokens(),
                "totalTokens", usage.getTotalTokens(),
                "estimatedCostUSD", String.format("$%.6f", totalCost),
                "model", "gpt-4o"
        );
    }

    // ─────────────────────────────────────────────────────────
    // Shared helper — builds consistent customer context string
    // Used across multiple methods — single source of truth
    // ─────────────────────────────────────────────────────────
    private String buildCustomerContext(Customer customer) {
        return """
                CUSTOMER RECORD:
                Name:           %s %s
                Email:          %s
                Plan:           %s
                Status:         %s
                Country:        %s | City: %s
                Last login:     %s (%d days ago)
                Login count:    %d
                Monthly spend:  $%.2f
                Lifetime value: $%.2f
                Open tickets:   %d | Total tickets: %d
                Notes:          %s
                """.formatted(
                customer.getFirstName(), customer.getLastName(),
                customer.getEmail(),
                customer.getPlan(),
                customer.getStatus(),
                customer.getCountry(), customer.getCity(),
                customer.getLastLoginAt(),
                getDaysSinceLogin(customer),
                customer.getLoginCount(),
                customer.getMonthlySpend(),
                customer.getLifetimeValue(),
                customer.getOpenTickets(), customer.getTotalTickets(),
                customer.getNotes() != null ? customer.getNotes() : "None"
        );
    }

    private long getDaysSinceLogin(Customer customer) {
        if (customer.getLastLoginAt() == null) return 999L;
        return ChronoUnit.DAYS.between(
                customer.getLastLoginAt().toLocalDate(),
                java.time.LocalDate.now()
        );
    }
}