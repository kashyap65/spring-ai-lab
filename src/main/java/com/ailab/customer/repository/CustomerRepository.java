package com.ailab.customer.repository;

import com.ailab.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CustomerRepository
 *
 * Standard Spring Data JPA repo.
 * The custom queries here will be called by the AI function-calling
 * tools we wire up in Hour 4. The AI model will "decide" to call
 * these methods based on the user's natural language request.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // ── Basic finders ──────────────────────────────────────────

    Optional<Customer> findByEmail(String email);

    List<Customer> findByPlan(Customer.Plan plan);

    List<Customer> findByStatus(Customer.Status status);

    List<Customer> findByCountry(String country);

    // ── Churn detection queries ────────────────────────────────

    /**
     * Find customers who haven't logged in since a given date.
     * Used in Hour 4 function-calling: AI calls this when user asks
     * "who hasn't logged in for 30 days?"
     */
    @Query("SELECT c FROM Customer c WHERE c.lastLoginAt < :since AND c.status = 'ACTIVE'")
    List<Customer> findInactiveCustomersSince(@Param("since") OffsetDateTime since);

    /**
     * Premium customers at churn risk (no login > N days).
     * High-value query for AI-powered customer success.
     */
    @Query("""
        SELECT c FROM Customer c
        WHERE c.plan = 'PREMIUM'
          AND c.status = 'ACTIVE'
          AND c.lastLoginAt < :since
        ORDER BY c.lifetimeValue DESC
        """)
    List<Customer> findPremiumChurnRisk(@Param("since") OffsetDateTime since);

    // ── Engagement & analytics ─────────────────────────────────

    /**
     * Find customers by plan and status.
     * AI uses this for filtered segment queries.
     */
    List<Customer> findByPlanAndStatus(Customer.Plan plan, Customer.Status status);

    /**
     * Top customers by lifetime value — used in AI "who are our best customers?" queries.
     */
    @Query("SELECT c FROM Customer c ORDER BY c.lifetimeValue DESC LIMIT :limit")
    List<Customer> findTopCustomersByValue(@Param("limit") int limit);

    /**
     * Customers with open support tickets — used for AI support prioritisation.
     */
    @Query("SELECT c FROM Customer c WHERE c.openTickets > 0 ORDER BY c.openTickets DESC")
    List<Customer> findCustomersWithOpenTickets();

    // ── Upsell candidates ──────────────────────────────────────

    /**
     * BASIC customers with high engagement (upgrade candidates).
     * AI uses this in Hour 4 for "who should we upsell?"
     */
    @Query("""
        SELECT c FROM Customer c
        WHERE c.plan = 'BASIC'
          AND c.status = 'ACTIVE'
          AND c.loginCount > :minLoginCount
        ORDER BY c.loginCount DESC
        """)
    List<Customer> findBasicUpgradeCandidates(@Param("minLoginCount") int minLoginCount);

    // ── Statistics ─────────────────────────────────────────────

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.plan = :plan")
    long countByPlan(@Param("plan") Customer.Plan plan);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status")
    long countByStatus(@Param("status") Customer.Status status);

    @Query("SELECT SUM(c.monthlySpend) FROM Customer c WHERE c.status = 'ACTIVE'")
    java.math.BigDecimal sumMonthlyRevenue();
}
