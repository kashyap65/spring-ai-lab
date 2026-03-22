package com.ailab.customer.controller;

import com.ailab.customer.ai.CustomerChatService;
import com.ailab.customer.ai.dto.ChurnRiskResult;
import com.ailab.customer.ai.dto.CustomerSummary;
import com.ailab.customer.ai.dto.UpsellOpportunity;
import com.ailab.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;

/**
 * CustomerAiController — Hour 3
 * <p>
 * All 4 ChatClient response modes exposed as REST endpoints.
 * <p>
 * Test URLs:
 * POST /api/ai/chat                   — plain text chat
 * GET  /api/ai/summary/{id}           — structured CustomerSummary
 * GET  /api/ai/churn-risk/{id}        — structured ChurnRiskResult
 * GET  /api/ai/upsell/{id}            — structured UpsellOpportunity
 * POST /api/ai/ask/{id}               — Q&A about one customer
 * GET  /api/ai/stream/chat            — streaming plain text
 * GET  /api/ai/stream/analyse/{id}    — streaming customer report
 * POST /api/ai/chat/metadata          — response with token counts
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class CustomerAiController {

    private final CustomerChatService chatService;
    private final CustomerService customerService;

    // ── Mode 1: String responses ───────────────────────────

    /**
     * POST /api/ai/chat
     * Body: {"message": "How many premium customers do we have?"}
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "Give me a customer overview");
        String response = chatService.chat(message);
        return ResponseEntity.ok(Map.of(
                "question", message,
                "answer", response
        ));
    }

    /**
     * POST /api/ai/ask/{id}
     * Body: {"question": "What plan is this customer on?"}
     */
    @PostMapping("/ask/{id}")
    public ResponseEntity<Map<String, String>> askAboutCustomer(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "Summarise this customer");
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(Map.of(
                        "customer", c.getFirstName() + " " + c.getLastName(),
                        "question", question,
                        "answer", chatService.askAboutCustomer(c, question)
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Mode 2: Structured output → Java records ───────────

    /**
     * GET /api/ai/summary/{id} → CustomerSummary record as JSON
     */
    @GetMapping("/summary/{id}")
    public ResponseEntity<CustomerSummary> getStructuredSummary(
            @PathVariable UUID id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(chatService.getStructuredSummary(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/ai/churn-risk/{id} → ChurnRiskResult record as JSON
     */
    @GetMapping("/churn-risk/{id}")
    public ResponseEntity<ChurnRiskResult> getChurnRisk(
            @PathVariable UUID id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(chatService.assessChurnRisk(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/ai/upsell/{id} → UpsellOpportunity record as JSON
     */
    @GetMapping("/upsell/{id}")
    public ResponseEntity<UpsellOpportunity> getUpsellOpportunity(
            @PathVariable UUID id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(chatService.analyseUpsellOpportunity(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Mode 3: Streaming → Server-Sent Events ─────────────

    /**
     * GET /api/ai/stream/chat?message=...
     * Returns tokens one-by-one as Server-Sent Events.
     * Open in browser — watch tokens arrive like ChatGPT.
     * <p>
     * MediaType.TEXT_EVENT_STREAM_VALUE = "text/event-stream"
     * This is the SSE content type — browser handles it natively.
     */
    @GetMapping(value = "/stream/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam(defaultValue = "Analyse our customer health") String message) {
        return chatService.streamChat(message);
    }

    /**
     * GET /api/ai/stream/analyse/{id}
     * Streams a detailed customer report token by token.
     */
    @GetMapping(value = "/stream/analyse/{id}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAnalysis(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(chatService::streamCustomerAnalysis)
                .orElse(Flux.just("Customer not found"));
    }

    // ── Mode 4: Full metadata including token counts ────────

    /**
     * POST /api/ai/chat/metadata
     * Body: {"question": "What is our MRR?"}
     * Returns answer + token counts + estimated cost.
     */
    @PostMapping("/chat/metadata")
    public ResponseEntity<Map<String, Object>> chatWithMetadata(
            @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "Summarise our customers");
        return ResponseEntity.ok(chatService.chatWithMetadata(question));
    }
}