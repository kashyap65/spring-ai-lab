package com.ailab.customer.controller;

import com.ailab.customer.ai.FunctionCallingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FunctionCallingController — Hour 4
 * <p>
 * REST endpoints for AI function calling.
 * Each endpoint passes a natural language question
 * to the AI — the model decides which tools to call.
 * <p>
 * Test endpoints:
 * POST /api/ai/tools/ask
 * GET  /api/ai/tools/churn-analysis
 * GET  /api/ai/tools/revenue-growth
 * GET  /api/ai/tools/support-health
 */
@RestController
@RequestMapping("/api/ai/tools")
@RequiredArgsConstructor
public class FunctionCallingController {

    private final FunctionCallingService functionCallingService;

    /**
     * POST /api/ai/tools/ask
     * Body: {"question": "Which enterprise customers have open tickets?"}
     * <p>
     * The most powerful endpoint — ask ANYTHING in natural language.
     * The model decides which combination of tools to call.
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(
            @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question",
                "Give me a full customer health overview");
        String answer = functionCallingService.ask(question);
        return ResponseEntity.ok(Map.of(
                "question", question,
                "answer", answer
        ));
    }

    /**
     * GET /api/ai/tools/churn-analysis?days=30
     * AI fetches at-risk customers and composes prioritised report.
     */
    @GetMapping("/churn-analysis")
    public ResponseEntity<Map<String, String>> churnAnalysis(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(Map.of(
                "analysis", functionCallingService.analyseChurnRisk(days)
        ));
    }

    /**
     * GET /api/ai/tools/revenue-growth
     * AI cross-references upsell candidates with top customers.
     */
    @GetMapping("/revenue-growth")
    public ResponseEntity<Map<String, String>> revenueGrowth() {
        return ResponseEntity.ok(Map.of(
                "analysis", functionCallingService.analyseRevenueGrowth()
        ));
    }

    /**
     * GET /api/ai/tools/support-health
     * AI prioritises support queue by customer value.
     */
    @GetMapping("/support-health")
    public ResponseEntity<Map<String, String>> supportHealth() {
        return ResponseEntity.ok(Map.of(
                "analysis", functionCallingService.analyseSupportHealth()
        ));
    }
}