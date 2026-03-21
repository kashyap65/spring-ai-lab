package com.ailab.customer.service;

import com.ailab.customer.model.Customer;
import com.ailab.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CustomerService — core business logic.
 *
 * This service is completely unaware of AI.
 * In Hour 4, the AI layer will call these methods
 * through @Tool function calling.
 *
 * Design principle: keep business logic clean and testable
 * independent of AI. AI is just another caller.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    // ── CRUD ───────────────────────────────────────────────────

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(UUID id) {
        customerRepository.deleteById(id);
    }

    // ── Plan & Status queries ──────────────────────────────────

    public List<Customer> findByPlan(Customer.Plan plan) {
        log.debug("Finding customers by plan: {}", plan);
        return customerRepository.findByPlan(plan);
    }

    public List<Customer> findByStatus(Customer.Status status) {
        return customerRepository.findByStatus(status);
    }

    public List<Customer> findByPlanAndStatus(Customer.Plan plan, Customer.Status status) {
        return customerRepository.findByPlanAndStatus(plan, status);
    }

    // ── Churn detection ────────────────────────────────────────

    /**
     * Find active customers who haven't logged in for N days.
     *
     * This method is called by the AI tool in Hour 4 when
     * a user asks: "who hasn't logged in for 30 days?"
     */
    public List<Customer> findInactiveSince(int days) {
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(days);
        log.debug("Finding customers inactive since {} ({} days ago)", threshold, days);
        return customerRepository.findInactiveCustomersSince(threshold);
    }

    public List<Customer> findPremiumChurnRisk(int days) {
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(days);
        return customerRepository.findPremiumChurnRisk(threshold);
    }

    // ── Engagement ─────────────────────────────────────────────

    public List<Customer> findTopCustomers(int limit) {
        return customerRepository.findTopCustomersByValue(limit);
    }

    public List<Customer> findCustomersWithOpenTickets() {
        return customerRepository.findCustomersWithOpenTickets();
    }

    public List<Customer> findUpgradeCandidates(int minLoginCount) {
        return customerRepository.findBasicUpgradeCandidates(minLoginCount);
    }

    // ── Stats (used by AI for summary answers) ─────────────────

    public CustomerStats getStats() {
        return new CustomerStats(
            customerRepository.countByPlan(Customer.Plan.FREE),
            customerRepository.countByPlan(Customer.Plan.BASIC),
            customerRepository.countByPlan(Customer.Plan.PREMIUM),
            customerRepository.countByPlan(Customer.Plan.ENTERPRISE),
            customerRepository.countByStatus(Customer.Status.ACTIVE),
            customerRepository.countByStatus(Customer.Status.CHURNED),
            customerRepository.sumMonthlyRevenue()
        );
    }

    // ── Inner record for stats ─────────────────────────────────

    public record CustomerStats(
        long freeCount,
        long basicCount,
        long premiumCount,
        long enterpriseCount,
        long activeCount,
        long churnedCount,
        java.math.BigDecimal monthlyRevenue
    ) {
        public long totalCustomers() {
            return freeCount + basicCount + premiumCount + enterpriseCount;
        }
    }
}
