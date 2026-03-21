package com.ailab.customer.controller;

import com.ailab.customer.ai.PromptEngineeringService;
import com.ailab.customer.model.Customer;
import com.ailab.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PromptEngineeringController — Hour 2 endpoints
 *
 * Each endpoint demonstrates one prompt technique.
 * Test them via browser or Postman after starting the app.
 */
@RestController
@RequestMapping("/api/prompt")
@RequiredArgsConstructor
public class PromptEngineeringController {

    private final PromptEngineeringService promptService;
    private final CustomerService customerService;

    // Technique 1 — Zero-shot summary
    // GET http://localhost:8080/api/prompt/summarise/{customerId}
    @GetMapping("/summarise/{id}")
    public ResponseEntity<Map<String, String>> summarise(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(Map.of(
                        "technique", "zero-shot",
                        "result", promptService.zeroShot_summariseCustomer(c)
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // Technique 2 — Few-shot churn classification
    // GET http://localhost:8080/api/prompt/churn-risk/{customerId}
    @GetMapping("/churn-risk/{id}")
    public ResponseEntity<Map<String, String>> churnRisk(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(Map.of(
                        "technique", "few-shot",
                        "customer",  c.getFirstName() + " " + c.getLastName(),
                        "result",    promptService.fewShot_classifyChurnRisk(c)
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // Technique 3 — Chain-of-thought upsell analysis
    // GET http://localhost:8080/api/prompt/upsell/{customerId}
    @GetMapping("/upsell/{id}")
    public ResponseEntity<Map<String, String>> upsell(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(Map.of(
                        "technique", "chain-of-thought",
                        "customer",  c.getFirstName() + " " + c.getLastName(),
                        "result",    promptService.chainOfThought_upsellAnalysis(c)
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // Technique 4 — PromptTemplate email generation
    // GET http://localhost:8080/api/prompt/email/{id}?type=re-engagement
    @GetMapping("/email/{id}")
    public ResponseEntity<Map<String, String>> generateEmail(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "re-engagement") String type) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(Map.of(
                        "technique", "prompt-template",
                        "emailType", type,
                        "result",    promptService.promptTemplate_generateEmail(c, type)
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // Technique 5 — Role + constraints segmentation
    // GET http://localhost:8080/api/prompt/segment?plan=PREMIUM
    @GetMapping("/segment")
    public ResponseEntity<Map<String, String>> segment(
            @RequestParam(defaultValue = "PREMIUM") Customer.Plan plan) {
        List<Customer> customers = customerService.findByPlan(plan);
        return ResponseEntity.ok(Map.of(
                "technique",     "role-and-constraints",
                "customerCount", String.valueOf(customers.size()),
                "result",        promptService.roleAndConstraints_segmentCustomers(customers)
        ));
    }

    // Technique 6 — Context injection Q&A
    // POST http://localhost:8080/api/prompt/ask/{id}
    // Body: {"question": "What plan is this customer on?"}
    @PostMapping("/ask/{id}")
    public ResponseEntity<Map<String, String>> ask(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "Summarise this customer");
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(Map.of(
                        "technique", "context-injection",
                        "question",  question,
                        "result",    promptService.contextInjection_answerQuestion(c, question)
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}