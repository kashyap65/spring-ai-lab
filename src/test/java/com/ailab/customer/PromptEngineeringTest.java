package com.ailab.customer;

import com.ailab.customer.ai.PromptEngineeringService;
import com.ailab.customer.model.Customer;
import com.ailab.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PromptEngineeringTest {

    @Autowired PromptEngineeringService promptService;
    @Autowired CustomerService customerService;

    @Test
    @DisplayName("Technique 1 -- zero-shot summary is not blank")
    void zeroShot_summary() {
        Customer c = customerService.findByPlan(Customer.Plan.PREMIUM).get(0);
        String result = promptService.zeroShot_summariseCustomer(c);
        System.out.println("ZERO-SHOT:\n" + result);
        assertThat(result).isNotBlank();
    }

    @Test
    @DisplayName("Technique 2 -- few-shot returns valid JSON with risk field")
    void fewShot_churnRisk() {
        Customer c = customerService.findByPlan(Customer.Plan.PREMIUM).get(0);
        String result = promptService.fewShot_classifyChurnRisk(c);
        System.out.println("FEW-SHOT:\n" + result);
        assertThat(result).contains("risk");
        assertThat(result).containsAnyOf("LOW", "MEDIUM", "HIGH");
    }

    @Test
    @DisplayName("Technique 3 -- chain-of-thought shows step by step reasoning")
    void chainOfThought_upsell() {
        Customer c = customerService.findByPlan(Customer.Plan.BASIC).get(0);
        String result = promptService.chainOfThought_upsellAnalysis(c);
        System.out.println("CHAIN-OF-THOUGHT:\n" + result);
        assertThat(result).isNotBlank();
        assertThat(result.toLowerCase()).containsAnyOf("step", "plan", "recommend");
    }

    @Test
    @DisplayName("Technique 4 -- prompt template generates email")
    void promptTemplate_email() {
        Customer c = customerService.findByPlan(Customer.Plan.PREMIUM).get(2);
        String result = promptService.promptTemplate_generateEmail(c, "re-engagement");
        System.out.println("PROMPT TEMPLATE:\n" + result);
        assertThat(result).isNotBlank();
        assertThat(result).contains(c.getFirstName());
    }

    @Test
    @DisplayName("Technique 5 -- role+constraints segments all premium customers")
    void roleConstraints_segment() {
        List<Customer> customers = customerService.findByPlan(Customer.Plan.PREMIUM);
        String result = promptService.roleAndConstraints_segmentCustomers(customers);
        System.out.println("ROLE+CONSTRAINTS:\n" + result);
        assertThat(result).contains("segment");
        assertThat(result).containsAnyOf(
                "CHAMPION", "HEALTHY", "AT_RISK", "CHURNING", "NEEDS_ONBOARDING"
        );
    }

    @Test
    @DisplayName("Technique 6 -- context injection answers from data only")
    void contextInjection_qa() {
        Customer c = customerService.findByPlan(Customer.Plan.PREMIUM).get(0);
        String result = promptService.contextInjection_answerQuestion(
                c, "What is this customer's monthly spend and when did they last log in?"
        );
        System.out.println("CONTEXT INJECTION:\n" + result);
        assertThat(result).isNotBlank();
    }
}