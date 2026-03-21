package com.ailab.customer.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Customer entity — mirrors V1 Flyway schema exactly.
 *
 * Plan enum:   FREE | BASIC | PREMIUM | ENTERPRISE
 * Status enum: ACTIVE | INACTIVE | SUSPENDED | CHURNED
 *
 * The `notes` field is the free-text column we'll use
 * for RAG semantic search in Hours 5 & 6.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    // ── Subscription ───────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Plan plan;

    @Column(name = "plan_start_date")
    private LocalDate planStartDate;

    @Column(name = "plan_end_date")
    private LocalDate planEndDate;

    // ── Status ─────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    // ── Engagement ─────────────────────────────────────────
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "login_count", nullable = false)
    @Builder.Default
    private Integer loginCount = 0;

    // ── Location ───────────────────────────────────────────
    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    // ── Support ────────────────────────────────────────────
    @Column(name = "open_tickets", nullable = false)
    @Builder.Default
    private Integer openTickets = 0;

    @Column(name = "total_tickets", nullable = false)
    @Builder.Default
    private Integer totalTickets = 0;

    // ── Financials ─────────────────────────────────────────
    @Column(name = "lifetime_value", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lifetimeValue = BigDecimal.ZERO;

    @Column(name = "monthly_spend", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal monthlySpend = BigDecimal.ZERO;

    // ── Notes — used for RAG semantic search ───────────────
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Audit ──────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ── Helper: full name ──────────────────────────────────
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ── Enums ──────────────────────────────────────────────
    public enum Plan {
        FREE, BASIC, PREMIUM, ENTERPRISE
    }

    public enum Status {
        ACTIVE, INACTIVE, SUSPENDED, CHURNED
    }
}
