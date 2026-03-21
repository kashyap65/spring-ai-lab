package com.ailab.customer.ai;

import com.ailab.customer.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * PromptEngineeringService — Hour 2
 *
 * Demonstrates all 6 prompt engineering techniques
 * applied to real customer data operations.
 *
 * Each method = one technique. Read them in order.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromptEngineeringService {

    private final ChatClient chatClient;

    // ─────────────────────────────────────────────────────────
    // TECHNIQUE 1: Zero-shot
    // Direct question, no examples, no structure
    // Use for: simple, clear tasks
    // ─────────────────────────────────────────────────────────
    public String zeroShot_summariseCustomer(Customer customer) {
        String prompt = """
            Summarise this customer in 2 sentences for a sales team briefing:
            
            Name:         %s %s
            Plan:         %s
            Status:       %s
            Last login:   %s
            Login count:  %d
            Monthly spend: $%.2f
            Open tickets: %d
            Notes:        %s
            """.formatted(
                customer.getFirstName(), customer.getLastName(),
                customer.getPlan(),
                customer.getStatus(),
                customer.getLastLoginAt(),
                customer.getLoginCount(),
                customer.getMonthlySpend(),
                customer.getOpenTickets(),
                customer.getNotes()
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    // ─────────────────────────────────────────────────────────
    // TECHNIQUE 2: Few-shot
    // Show examples so model learns the exact output pattern
    // Use for: classification, strict format requirements
    // ─────────────────────────────────────────────────────────
    public String fewShot_classifyChurnRisk(Customer customer) {

        // Few-shot examples teach the model the exact format we want
        String systemPrompt = """
            You classify customer churn risk. 
            Return ONLY a JSON object. No explanation. No markdown.
            
            Examples:
            
            Input: Plan=PREMIUM, DaysSinceLogin=2, OpenTickets=0, LoginCount=200
            Output: {"risk":"LOW","confidence":0.95,"reason":"Highly active premium user"}
            
            Input: Plan=PREMIUM, DaysSinceLogin=45, OpenTickets=2, LoginCount=89
            Output: {"risk":"HIGH","confidence":0.88,"reason":"Extended inactivity with unresolved tickets"}
            
            Input: Plan=BASIC, DaysSinceLogin=12, OpenTickets=0, LoginCount=34
            Output: {"risk":"LOW","confidence":0.75,"reason":"Normal engagement for basic plan"}
            
            Input: Plan=PREMIUM, DaysSinceLogin=60, OpenTickets=3, LoginCount=44
            Output: {"risk":"HIGH","confidence":0.92,"reason":"Critical: long inactivity plus multiple open issues"}
            """;

        // Calculate days since last login
        long daysSinceLogin = customer.getLastLoginAt() != null
                ? java.time.temporal.ChronoUnit.DAYS.between(
                customer.getLastLoginAt().toLocalDate(),
                java.time.LocalDate.now())
                : 999;

        String userMessage = """
            Input: Plan=%s, DaysSinceLogin=%d, OpenTickets=%d, LoginCount=%d
            Output:
            """.formatted(
                customer.getPlan(),
                daysSinceLogin,
                customer.getOpenTickets(),
                customer.getLoginCount()
        );

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }

    // ─────────────────────────────────────────────────────────
    // TECHNIQUE 3: Chain-of-thought
    // Force the model to reason step by step
    // Use for: complex multi-factor analysis
    // ─────────────────────────────────────────────────────────
    public String chainOfThought_upsellAnalysis(Customer customer) {

        String prompt = """
            Analyse whether this customer should be upsold to a higher plan.
            
            Think through this step by step:
            Step 1 -- Current plan and its limitations
            Step 2 -- Engagement signals (login frequency, usage patterns)
            Step 3 -- Financial signals (current spend, lifetime value)
            Step 4 -- Support signals (ticket history)
            Step 5 -- Final recommendation with confidence score (0.0 to 1.0)
            
            CUSTOMER DATA:
            Name:          %s %s
            Current plan:  %s
            Login count:   %d
            Last login:    %s days ago
            Monthly spend: $%.2f
            Lifetime value:$%.2f
            Open tickets:  %d
            Notes:         %s
            """.formatted(
                customer.getFirstName(), customer.getLastName(),
                customer.getPlan(),
                customer.getLoginCount(),
                java.time.temporal.ChronoUnit.DAYS.between(
                        customer.getLastLoginAt().toLocalDate(),
                        java.time.LocalDate.now()),
                customer.getMonthlySpend(),
                customer.getLifetimeValue(),
                customer.getOpenTickets(),
                customer.getNotes()
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    // ─────────────────────────────────────────────────────────
    // TECHNIQUE 4: PromptTemplate — parameterised prompts
    // Reusable prompt with {variables} filled at runtime
    // Use for: prompts you reuse with different data
    // Java dev analogy: PreparedStatement for prompts
    // ─────────────────────────────────────────────────────────
    public String promptTemplate_generateEmail(Customer customer, String emailType) {

        // PromptTemplate is Spring AI's PreparedStatement for prompts
        // {variables} are replaced at call time — safe, reusable, testable
        PromptTemplate template = new PromptTemplate("""
            Write a {emailType} email to {customerName}.
            
            Customer context:
            - Plan: {plan}
            - Last login: {daysSinceLogin} days ago
            - Notes: {notes}
            
            Rules:
            - Maximum 3 sentences
            - Professional but warm tone
            - Include one specific reference to their usage pattern
            - End with a clear call to action
            - Do NOT mention competitors
            - Do NOT make up features that don't exist
            """);

        long daysSinceLogin = java.time.temporal.ChronoUnit.DAYS.between(
                customer.getLastLoginAt().toLocalDate(),
                java.time.LocalDate.now());

        // Variables injected safely — no string concatenation
        Map<String, Object> variables = Map.of(
                "emailType",      emailType,
                "customerName",   customer.getFirstName(),
                "plan",           customer.getPlan().toString(),
                "daysSinceLogin", daysSinceLogin,
                "notes",          customer.getNotes() != null
                        ? customer.getNotes() : "No notes available"
        );

        return chatClient.prompt()
                .user(template.render(variables))
                .call()
                .content();
    }

    // ─────────────────────────────────────────────────────────
    // TECHNIQUE 5: Role + constraints (system prompt)
    // Define persona + hard rules the model must follow
    // Use for: ALL production features — always do this
    // ─────────────────────────────────────────────────────────
    public String roleAndConstraints_segmentCustomers(List<Customer> customers) {

        // Build a compact CSV summary of customers
        StringBuilder customerData = new StringBuilder();
        customerData.append("Name,Plan,DaysSinceLogin,MonthlySpend,OpenTickets\n");
        for (Customer c : customers) {
            long days = c.getLastLoginAt() != null
                    ? java.time.temporal.ChronoUnit.DAYS.between(
                    c.getLastLoginAt().toLocalDate(), java.time.LocalDate.now())
                    : 999;
            customerData.append("%s %s,%s,%d,%.0f,%d\n".formatted(
                    c.getFirstName(), c.getLastName(),
                    c.getPlan(), days,
                    c.getMonthlySpend(), c.getOpenTickets()
            ));
        }

        String systemPrompt = """
            You are a B2B SaaS customer success analyst.
            
            Your rules:
            1. Only use the data provided. Never invent or assume facts.
            2. Categorise every customer into exactly one segment:
               CHAMPION, HEALTHY, AT_RISK, CHURNING, NEEDS_ONBOARDING
            3. Return ONLY a JSON array. No explanation. No markdown fences.
            4. Format: [{"name":"...","segment":"...","reason":"max 10 words"}]
            5. If you cannot determine a segment, use UNKNOWN.
            """;

        String userMessage = "Segment these customers:\n\n" + customerData;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }

    // ─────────────────────────────────────────────────────────
    // TECHNIQUE 6: Context injection
    // Inject real DB data into prompt at runtime
    // This is the foundation of RAG (Hours 5 & 6)
    // ─────────────────────────────────────────────────────────
    public String contextInjection_answerQuestion(
            Customer customer, String question) {

        // Note: the model answers ONLY from injected context
        // It cannot invent the customer's email or plan
        // This prevents hallucination for data-specific questions
        String prompt = """
            CUSTOMER RECORD (answer only from this data):
            ------------------------------------------------
            Name:          %s %s
            Email:         %s
            Plan:          %s
            Status:        %s
            Country:       %s
            Last login:    %s
            Login count:   %d
            Monthly spend: $%.2f
            Lifetime value:$%.2f
            Open tickets:  %d
            Total tickets: %d
            Notes:         %s
            ------------------------------------------------
            
            QUESTION: %s
            
            If the answer is not in the customer record above,
            say "I don't have that information in the customer record."
            Never guess or invent data.
            """.formatted(
                customer.getFirstName(), customer.getLastName(),
                customer.getEmail(),
                customer.getPlan(),
                customer.getStatus(),
                customer.getCountry(),
                customer.getLastLoginAt(),
                customer.getLoginCount(),
                customer.getMonthlySpend(),
                customer.getLifetimeValue(),
                customer.getOpenTickets(),
                customer.getTotalTickets(),
                customer.getNotes(),
                question
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}