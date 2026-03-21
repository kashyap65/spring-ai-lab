package com.ailab.customer;

import com.ailab.customer.model.Customer;
import com.ailab.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AiSetupVerificationTest
 *
 * Run this after docker compose up -d to verify:
 *   1. Spring Boot context starts cleanly
 *   2. PostgreSQL is connected and Flyway ran migrations
 *   3. 50 seed customers are in the DB
 *   4. OpenAI API key is valid and model responds
 *
 * Command: mvn test -Dtest=AiSetupVerificationTest
 */
@SpringBootTest
class AiSetupVerificationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ChatClient chatClient;

    @Test
    @DisplayName("1. Spring context loads and Flyway migrations ran")
    void contextLoads() {
        // If this passes — Spring Boot started cleanly
        System.out.println("✅ Spring Boot context started successfully");
    }

    @Test
    @DisplayName("2. PostgreSQL connected and 50 seed customers loaded")
    void databaseSeededCorrectly() {
        List<Customer> all = customerService.findAll();

        assertThat(all).hasSize(50);
        System.out.println("✅ Database has " + all.size() + " customers");

        // Verify plan distribution
        List<Customer> premium = customerService.findByPlan(Customer.Plan.PREMIUM);
        List<Customer> enterprise = customerService.findByPlan(Customer.Plan.ENTERPRISE);
        List<Customer> basic = customerService.findByPlan(Customer.Plan.BASIC);
        List<Customer> free = customerService.findByPlan(Customer.Plan.FREE);

        System.out.printf("   PREMIUM: %d | ENTERPRISE: %d | BASIC: %d | FREE: %d%n",
            premium.size(), enterprise.size(), basic.size(), free.size());

        assertThat(premium).isNotEmpty();
        assertThat(enterprise).isNotEmpty();
        assertThat(basic).isNotEmpty();
        assertThat(free).isNotEmpty();
    }

    @Test
    @DisplayName("3. Churn detection query works — find inactive > 30 days")
    void churnDetectionQueryWorks() {
        List<Customer> churnRisk = customerService.findPremiumChurnRisk(30);

        System.out.println("✅ Premium churn risk (>30 days inactive): " + churnRisk.size() + " customers");
        churnRisk.forEach(c ->
            System.out.printf("   - %s %s (last login: %s)%n",
                c.getFirstName(), c.getLastName(), c.getLastLoginAt()));
    }

    @Test
    @DisplayName("4. OpenAI API key is valid and GPT-4o responds")
    void openAiConnected() {
        // This call costs ~$0.001 — just one small prompt to verify connectivity
        String response = chatClient.prompt()
            .user("Reply with exactly: 'Spring AI lab is ready. Hour 1 complete.'")
            .call()
            .content();

        assertThat(response).isNotBlank();
        System.out.println("✅ OpenAI response: " + response);
    }

    @Test
    @DisplayName("5. Customer stats summary")
    void customerStatsSummary() {
        CustomerService.CustomerStats stats = customerService.getStats();

        System.out.println("✅ Customer base overview:");
        System.out.printf("   Total: %d | Active: %d | Churned: %d%n",
            stats.totalCustomers(), stats.activeCount(), stats.churnedCount());
        System.out.printf("   Monthly Revenue: $%.2f%n", stats.monthlyRevenue());
    }
}
