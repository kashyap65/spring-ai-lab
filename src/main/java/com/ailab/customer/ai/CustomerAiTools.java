package com.ailab.customer.ai;

import com.ailab.customer.model.Customer;
import com.ailab.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * CustomerAiTools — Hour 4
 *
 * These methods are registered as tools with the AI model.
 * GPT-4o reads the @Tool description and decides which
 * method to call based on the user's natural language request.
 *
 * KEY DESIGN PRINCIPLE:
 * These methods are thin wrappers — all real logic stays
 * in CustomerService. AI is just another caller.
 *
 * Java analogy: @Tool is like @RequestMapping for AI.
 * Just as Spring MVC routes HTTP requests to controllers,
 * Spring AI routes model decisions to these methods.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerAiTools {

    private final CustomerService customerService;

    // ─────────────────────────────────────────────────────────
    // TOOL 1 — Customer stats
    // Called when: "how many customers do we have?"
    //              "what is our MRR?"
    //              "give me a customer overview"
    // ─────────────────────────────────────────────────────────
    @Tool(description = """
        Get overall customer base statistics including total counts
        by plan (FREE, BASIC, PREMIUM, ENTERPRISE), active vs churned
        counts, and total monthly recurring revenue (MRR).
        Use this for overview questions about the customer base.
        """)
    public Map<String, Object> getCustomerStats() {
        log.info("AI called tool: getCustomerStats");
        CustomerService.CustomerStats stats = customerService.getStats();
        return Map.of(
                "totalCustomers",   stats.totalCustomers(),
                "freeCount",        stats.freeCount(),
                "basicCount",       stats.basicCount(),
                "premiumCount",     stats.premiumCount(),
                "enterpriseCount",  stats.enterpriseCount(),
                "activeCount",      stats.activeCount(),
                "churnedCount",     stats.churnedCount(),
                "monthlyRevenue",   stats.monthlyRevenue()
        );
    }

    // ─────────────────────────────────────────────────────────
    // TOOL 2 — Churn risk detection
    // Called when: "who hasn't logged in for 30 days?"
    //              "find inactive premium customers"
    //              "who is at churn risk?"
    // ─────────────────────────────────────────────────────────
    @Tool(description = """
        Find PREMIUM customers who have not logged in for at least
        the specified number of days and are at churn risk.
        Returns customer names, last login date, and lifetime value.
        Use for churn detection and re-engagement analysis.
        """)
    public List<Map<String, Object>> findPremiumChurnRisk(
            @ToolParam(description = "minimum number of days since last login, e.g. 30")
            int days) {

        log.info("AI called tool: findPremiumChurnRisk({})", days);
        return customerService.findPremiumChurnRisk(days)
                .stream()
                .map(c -> Map.<String, Object>of(
                        "name",          c.getFirstName() + " " + c.getLastName(),
                        "email",         c.getEmail(),
                        "daysSinceLogin", daysSince(c),
                        "lifetimeValue", c.getLifetimeValue(),
                        "openTickets",   c.getOpenTickets(),
                        "notes",         c.getNotes() != null ? c.getNotes() : ""
                ))
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // TOOL 3 — Inactive customers (all plans)
    // Called when: "who hasn't been active recently?"
    //              "find all customers inactive for 60 days"
    // ─────────────────────────────────────────────────────────
    @Tool(description = """
        Find ALL active customers (any plan) who have not logged in
        for at least the specified number of days.
        Use for broad inactivity analysis across all plan tiers.
        """)
    public List<Map<String, Object>> findInactiveCustomers(
            @ToolParam(description = "minimum number of days since last login")
            int days) {

        log.info("AI called tool: findInactiveCustomers({})", days);
        return customerService.findInactiveSince(days)
                .stream()
                .map(c -> Map.<String, Object>of(
                        "name",          c.getFirstName() + " " + c.getLastName(),
                        "plan",          c.getPlan().toString(),
                        "daysSinceLogin", daysSince(c),
                        "monthlySpend",  c.getMonthlySpend()
                ))
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // TOOL 4 — Top customers by value
    // Called when: "who are our best customers?"
    //              "show me top 5 customers by revenue"
    //              "which customers should we prioritise?"
    // ─────────────────────────────────────────────────────────
    @Tool(description = """
        Find the top N customers ranked by lifetime value (highest first).
        Use for VIP identification, priority support decisions,
        and revenue concentration analysis.
        """)
    public List<Map<String, Object>> findTopCustomersByValue(
            @ToolParam(description = "number of top customers to return, e.g. 5 or 10")
            int limit) {

        log.info("AI called tool: findTopCustomersByValue({})", limit);
        return customerService.findTopCustomers(limit)
                .stream()
                .map(c -> Map.<String, Object>of(
                        "name",          c.getFirstName() + " " + c.getLastName(),
                        "plan",          c.getPlan().toString(),
                        "lifetimeValue", c.getLifetimeValue(),
                        "monthlySpend",  c.getMonthlySpend(),
                        "status",        c.getStatus().toString()
                ))
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // TOOL 5 — Upsell candidates
    // Called when: "who should we upsell?"
    //              "find BASIC customers ready to upgrade"
    //              "who is outgrowing their current plan?"
    // ─────────────────────────────────────────────────────────
    @Tool(description = """
        Find BASIC plan customers with high engagement (frequent logins)
        who are likely ready to upgrade to PREMIUM.
        Use for upsell campaign planning and sales prioritisation.
        """)
    public List<Map<String, Object>> findUpsellCandidates(
            @ToolParam(description = "minimum number of total logins to qualify, e.g. 40")
            int minLogins) {

        log.info("AI called tool: findUpsellCandidates({})", minLogins);
        return customerService.findUpgradeCandidates(minLogins)
                .stream()
                .map(c -> Map.<String, Object>of(
                        "name",        c.getFirstName() + " " + c.getLastName(),
                        "email",       c.getEmail(),
                        "loginCount",  c.getLoginCount(),
                        "monthlySpend", c.getMonthlySpend(),
                        "notes",       c.getNotes() != null ? c.getNotes() : ""
                ))
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // TOOL 6 — Customers with open tickets
    // Called when: "who has unresolved support tickets?"
    //              "find customers needing support attention"
    //              "support queue analysis"
    // ─────────────────────────────────────────────────────────
    @Tool(description = """
        Find all customers who currently have open (unresolved)
        support tickets, ordered by number of tickets descending.
        Use for support prioritisation and customer health analysis.
        """)
    public List<Map<String, Object>> findCustomersWithOpenTickets() {
        log.info("AI called tool: findCustomersWithOpenTickets");
        return customerService.findCustomersWithOpenTickets()
                .stream()
                .map(c -> Map.<String, Object>of(
                        "name",         c.getFirstName() + " " + c.getLastName(),
                        "plan",         c.getPlan().toString(),
                        "openTickets",  c.getOpenTickets(),
                        "totalTickets", c.getTotalTickets(),
                        "email",        c.getEmail()
                ))
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // TOOL 7 — Find customers by plan
    // Called when: "list all enterprise customers"
    //              "show me PREMIUM customers"
    // ─────────────────────────────────────────────────────────
    @Tool(description = """
        Find all customers on a specific subscription plan.
        Plan must be one of: FREE, BASIC, PREMIUM, ENTERPRISE.
        Use for plan-specific analysis and segmentation.
        """)
    public List<Map<String, Object>> findCustomersByPlan(
            @ToolParam(description = "subscription plan: FREE, BASIC, PREMIUM, or ENTERPRISE")
            String plan) {

        log.info("AI called tool: findCustomersByPlan({})", plan);
        Customer.Plan planEnum = Customer.Plan.valueOf(plan.toUpperCase());
        return customerService.findByPlan(planEnum)
                .stream()
                .map(c -> Map.<String, Object>of(
                        "name",         c.getFirstName() + " " + c.getLastName(),
                        "status",       c.getStatus().toString(),
                        "country",      c.getCountry() != null ? c.getCountry() : "",
                        "monthlySpend", c.getMonthlySpend(),
                        "loginCount",   c.getLoginCount()
                ))
                .toList();
    }

    // ── Helper ────────────────────────────────────────────────
    private long daysSince(Customer c) {
        if (c.getLastLoginAt() == null) return 999L;
        return java.time.temporal.ChronoUnit.DAYS.between(
                c.getLastLoginAt().toLocalDate(),
                java.time.LocalDate.now()
        );
    }
}