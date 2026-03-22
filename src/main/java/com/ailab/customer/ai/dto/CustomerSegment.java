// CustomerSegment.java
package com.ailab.customer.ai.dto;

import java.util.List;

public record CustomerSegment(
        String segmentName,        // CHAMPION | HEALTHY | AT_RISK | CHURNING
        List<String> customerNames,
        String segmentInsight,
        String recommendedCampaign
) {}