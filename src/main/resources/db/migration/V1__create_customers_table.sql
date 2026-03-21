-- V1__create_customers_table.sql
-- Flyway migration: baseline schema
-- Naming convention: V{version}__{description}.sql

CREATE TABLE IF NOT EXISTS customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name      VARCHAR(100)        NOT NULL,
    last_name       VARCHAR(100)        NOT NULL,
    email           VARCHAR(255)        NOT NULL UNIQUE,
    phone           VARCHAR(20),

    -- Subscription
    plan            VARCHAR(20)         NOT NULL DEFAULT 'FREE',
                                        -- FREE | BASIC | PREMIUM | ENTERPRISE
    plan_start_date DATE,
    plan_end_date   DATE,

    -- Status
    status          VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
                                        -- ACTIVE | INACTIVE | SUSPENDED | CHURNED
    -- Engagement
    last_login_at   TIMESTAMP WITH TIME ZONE,
    login_count     INTEGER             NOT NULL DEFAULT 0,

    -- Location
    country         VARCHAR(100),
    city            VARCHAR(100),

    -- Support
    open_tickets    INTEGER             NOT NULL DEFAULT 0,
    total_tickets   INTEGER             NOT NULL DEFAULT 0,

    -- Financials
    lifetime_value  NUMERIC(10, 2)      NOT NULL DEFAULT 0.00,
    monthly_spend   NUMERIC(10, 2)      NOT NULL DEFAULT 0.00,

    -- Notes — free-text field used in RAG examples
    notes           TEXT,

    -- Audit
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for common query patterns
CREATE INDEX idx_customers_plan    ON customers(plan);
CREATE INDEX idx_customers_status  ON customers(status);
CREATE INDEX idx_customers_email   ON customers(email);
CREATE INDEX idx_customers_last_login ON customers(last_login_at);

-- Auto-update updated_at on row change
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trg_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
