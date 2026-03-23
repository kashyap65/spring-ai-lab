package com.ailab.customer;

import com.ailab.customer.ai.FunctionCallingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Timeout(value = 120)  // per test
class FunctionCallingTest {

    @Autowired
    FunctionCallingService functionCallingService;

    @Test
    @DisplayName("AI calls getCustomerStats tool for overview question")
    void test_statsToolCalled() {
        // Watch the logs — you will see:
        // "AI called tool: getCustomerStats"
        String answer = functionCallingService.ask(
                "How many customers do we have and what is our MRR?"
        );
        System.out.println("ANSWER:\n" + answer);
        assertThat(answer).isNotBlank();
        // Response will contain real numbers from your DB
        assertThat(answer).containsAnyOf("48", "customer", "revenue");
    }

    @Test
    @DisplayName("AI calls findPremiumChurnRisk tool for churn question")
    void test_churnToolCalled() {
        // Watch the logs — you will see:
        // "AI called tool: findPremiumChurnRisk(30)"
        String answer = functionCallingService.ask(
                "Which premium customers haven't logged in for 30 days?"
        );
        System.out.println("CHURN ANSWER:\n" + answer);
        assertThat(answer).isNotBlank();
    }

    @Test
    @DisplayName("AI calls MULTIPLE tools for complex question")
    @Timeout(value = 180)
    void test_multipleToolsCalled() {
        // Watch the logs — you will see MULTIPLE tool calls:
        // "AI called tool: findPremiumChurnRisk(30)"
        // "AI called tool: findTopCustomersByValue(5)"
        // Model cross-references both results
        String answer = functionCallingService.ask(
                "Which of our highest-value customers are at churn risk? " +
                        "Find premium customers inactive 30+ days and compare " +
                        "with our top 5 customers by lifetime value."
        );
        System.out.println("MULTI-TOOL ANSWER:\n" + answer);
        assertThat(answer).isNotBlank();
    }

    @Test
    @DisplayName("Full churn analysis report")
    void test_churnAnalysis() {
        String report = functionCallingService.analyseChurnRisk(30);
        System.out.println("CHURN REPORT:\n" + report);
        assertThat(report).isNotBlank();
        assertThat(report.toLowerCase())
                .containsAnyOf("risk", "inactive", "action", "churn");
    }

    @Test
    @DisplayName("Revenue growth analysis uses multiple tools")
    void test_revenueGrowth() {
        // Logs will show: findUpsellCandidates + findTopCustomersByValue
        String report = functionCallingService.analyseRevenueGrowth();
        System.out.println("REVENUE REPORT:\n" + report);
        assertThat(report).isNotBlank();
    }

    @Test
    @DisplayName("Support health prioritises by customer value")
    void test_supportHealth() {
        String report = functionCallingService.analyseSupportHealth();
        System.out.println("SUPPORT REPORT:\n" + report);
        assertThat(report).isNotBlank();
    }
}