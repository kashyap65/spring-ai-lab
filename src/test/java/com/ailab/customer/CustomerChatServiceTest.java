package com.ailab.customer;

import com.ailab.customer.ai.CustomerChatService;
import com.ailab.customer.ai.dto.*;
import com.ailab.customer.model.Customer;
import com.ailab.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerChatServiceTest {

    @Autowired CustomerChatService chatService;
    @Autowired CustomerService customerService;

    @Test
    @DisplayName("Mode 1 -- chat() returns non-blank String answer")
    void mode1_chat() {
        String answer = chatService.chat("How many premium customers do we have?");
        System.out.println("CHAT ANSWER:\n" + answer);
        assertThat(answer).isNotBlank();
    }

    @Test
    @DisplayName("Mode 2 -- getStructuredSummary() returns typed record")
    void mode2_structuredSummary() {
        Customer c = customerService.findByPlan(Customer.Plan.PREMIUM).get(0);
        CustomerSummary summary = chatService.getStructuredSummary(c);

        System.out.println("STRUCTURED SUMMARY:");
        System.out.println("  Name:      " + summary.name());
        System.out.println("  Health:    " + summary.healthStatus());
        System.out.println("  Insight:   " + summary.keyInsight());
        System.out.println("  Action:    " + summary.recommendedAction());

        assertThat(summary.name()).isNotBlank();
        assertThat(summary.healthStatus())
                .isIn("HEALTHY", "AT_RISK", "CRITICAL");
        assertThat(summary.recommendedAction()).isNotBlank();
    }

    @Test
    @DisplayName("Mode 2 -- assessChurnRisk() returns typed ChurnRiskResult")
    void mode2_churnRisk() {
        // James Wilson — seeded as 45 days inactive — should be HIGH risk
        Customer c = customerService.findByPlan(Customer.Plan.PREMIUM)
                .stream()
                .filter(cu -> cu.getLastLoginAt() != null
                        && java.time.temporal.ChronoUnit.DAYS.between(
                        cu.getLastLoginAt().toLocalDate(),
                        java.time.LocalDate.now()) > 30)
                .findFirst()
                .orElse(customerService.findByPlan(Customer.Plan.PREMIUM).get(0));

        ChurnRiskResult risk = chatService.assessChurnRisk(c);

        System.out.println("CHURN RISK for " + c.getFirstName() + ":");
        System.out.println("  Risk level:  " + risk.riskLevel());
        System.out.println("  Confidence:  " + risk.confidenceScore());
        System.out.println("  Reason:      " + risk.primaryReason());
        System.out.println("  Days to churn estimate: " + risk.daysUntilChurnEstimate());
        System.out.println("  Action:      " + risk.suggestedAction());

        assertThat(risk.riskLevel())
                .isIn("LOW", "MEDIUM", "HIGH", "CRITICAL");
        assertThat(risk.confidenceScore()).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("Mode 2 -- analyseUpsellOpportunity() returns typed record")
    void mode2_upsell() {
        Customer c = customerService.findByPlan(Customer.Plan.BASIC).get(0);
        UpsellOpportunity opp = chatService.analyseUpsellOpportunity(c);

        System.out.println("UPSELL OPPORTUNITY for " + c.getFirstName() + ":");
        System.out.println("  Should upsell: " + opp.shouldUpsell());
        System.out.println("  Current plan:  " + opp.currentPlan());
        System.out.println("  Suggested:     " + opp.suggestedPlan());
        System.out.println("  Confidence:    " + opp.confidenceScore());
        System.out.println("  Pitch angle:   " + opp.pitchAngle());

        assertThat(opp.currentPlan()).isNotBlank();
    }

    @Test
    @DisplayName("Mode 3 -- streamChat() emits multiple tokens via Flux")
    void mode3_streaming() {
        Flux<String> tokens = chatService.streamChat("What is our biggest churn risk?");

        // StepVerifier -- Reactor's test utility for Flux
        // Like assertThat() but for async streams
        StepVerifier.create(tokens)
                .expectNextMatches(Objects::nonNull)  // first token arrives
                .thenConsumeWhile(token -> true)            // consume all remaining
                .verifyComplete();

        // Also collect and print to see full streamed response
        String fullResponse = tokens.collectList().block()
                .stream().reduce("", String::concat);
        System.out.println("STREAMED RESPONSE:\n" + fullResponse);
    }

    @Test
    @DisplayName("Mode 4 -- chatWithMetadata() returns token counts")
    void mode4_metadata() {
        Map<String, Object> result = chatService.chatWithMetadata(
                "How many customers are at churn risk?"
        );

        System.out.println("RESPONSE WITH METADATA:");
        System.out.println("  Answer:        " + result.get("answer"));
        System.out.println("  Input tokens:  " + result.get("inputTokens"));
        System.out.println("  Output tokens: " + result.get("outputTokens"));
        System.out.println("  Total tokens:  " + result.get("totalTokens"));
        System.out.println("  Cost:          " + result.get("estimatedCostUSD"));

        assertThat(result.get("answer")).isNotNull();
        assertThat((Integer) result.get("inputTokens")).isGreaterThan(0);
        assertThat(result.get("estimatedCostUSD").toString()).startsWith("$");
    }
}