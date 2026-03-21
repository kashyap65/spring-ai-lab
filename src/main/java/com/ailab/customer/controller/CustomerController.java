package com.ailab.customer.controller;

import com.ailab.customer.model.Customer;
import com.ailab.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CustomerController — standard REST API.
 *
 * Endpoints:
 *   GET  /api/customers                  — list all
 *   GET  /api/customers/{id}             — get by ID
 *   GET  /api/customers/plan/{plan}      — by plan
 *   GET  /api/customers/inactive/{days}  — churn risk
 *   GET  /api/customers/stats            — summary stats
 *
 * In Hour 3 we'll add:
 *   POST /api/customers/ai/chat          — natural language query
 *   POST /api/customers/ai/analyse/{id}  — AI customer analysis
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public List<Customer> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> findById(@PathVariable UUID id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plan/{plan}")
    public List<Customer> findByPlan(@PathVariable Customer.Plan plan) {
        return customerService.findByPlan(plan);
    }

    @GetMapping("/inactive/{days}")
    public List<Customer> findInactive(@PathVariable int days) {
        return customerService.findInactiveSince(days);
    }

    @GetMapping("/churn-risk")
    public List<Customer> premiumChurnRisk(
            @RequestParam(defaultValue = "30") int days) {
        return customerService.findPremiumChurnRisk(days);
    }

    @GetMapping("/stats")
    public CustomerService.CustomerStats getStats() {
        return customerService.getStats();
    }

    @GetMapping("/top")
    public List<Customer> topCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return customerService.findTopCustomers(limit);
    }
}
