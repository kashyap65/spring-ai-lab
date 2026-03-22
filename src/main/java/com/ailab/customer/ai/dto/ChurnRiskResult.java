// ChurnRiskResult.java
package com.ailab.customer.ai.dto;

public record ChurnRiskResult(
        String riskLevel,          // LOW | MEDIUM | HIGH | CRITICAL
        double confidenceScore,    // 0.0 to 1.0
        String primaryReason,      // main churn signal
        int daysUntilChurnEstimate,// estimated days before churn
        String suggestedAction     // what CSM should do
) {}