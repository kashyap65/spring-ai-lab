// UpsellOpportunity.java
package com.ailab.customer.ai.dto;

public record UpsellOpportunity(
        boolean shouldUpsell,
        String currentPlan,
        String suggestedPlan,      // BASIC | PREMIUM | ENTERPRISE
        double confidenceScore,
        String reason,
        String pitchAngle          // what to lead with in the conversation
) {}