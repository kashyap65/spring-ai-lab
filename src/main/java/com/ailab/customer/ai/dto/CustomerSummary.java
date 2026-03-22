// CustomerSummary.java
package com.ailab.customer.ai.dto;

/**
 * Structured output record for customer summary.
 * Spring AI maps GPT-4o JSON response → this record automatically.
 *
 * Java analogy: like a DTO that RestTemplate deserialises for you.
 */
public record CustomerSummary(
        String name,
        String plan,
        String healthStatus,       // HEALTHY | AT_RISK | CRITICAL
        String keyInsight,         // one sentence insight
        String recommendedAction   // one concrete next step
) {}