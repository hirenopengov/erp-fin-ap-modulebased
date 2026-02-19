-- ============================================
-- PAYMENT RUN DATABASE DESIGN
-- ============================================
-- Based on: https://opengovinc.atlassian.net/wiki/spaces/FINENG/pages/5810553309/Payment+Run+Database+Design
-- 
-- This SQL script defines the complete database schema for the Payment Run feature.
-- It includes all tables, enums, indexes, and constraints required for payment processing.
--
-- Tables:
--   - ap_bank_account: Bank accounts used for payments
--   - ap_remittance_message_template: Remittance message templates
--   - ap_payment_run: Main payment run master table
--   - ap_payment_run_invoice: Relationship table linking payment runs to invoices
--   - ap_payment_register: Payment register (one per vendor per payment run)
--   - ap_payment_register_line: Payment lines linking registers to invoices
--   - ap_payment_run_job: Spring Batch job execution tracking
--   - ap_payment_run_transition: Payment run status transition history
--
-- ============================================

-- ============================================
-- ENUM TYPES
-- ============================================
-- All enumeration types are defined here at the top to ensure they exist
-- before being referenced in table definitions.

-- Payment run type enumeration
CREATE TYPE IF NOT EXISTS ap_payment_run_type AS ENUM (
    'REGULAR',
    'MANUAL',
    'EMERGENCY',
    'INTEREST'
);

-- Payment method enumeration (used in payment_run and payment_register)
CREATE TYPE IF NOT EXISTS ap_payment_method AS ENUM (
    'CHECK',
    'ACH',
    'WIRE'
);

-- Payment run status enumeration
CREATE TYPE IF NOT EXISTS ap_payment_run_status AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'PREPARING',
    'PENDING_APPROVAL',
    'APPROVED',
    'REJECTED',
    'PROCESSING',
    'ISSUED',
    'COMPLETED',
    'CANCELLED',
    'FAILED'
);

-- Payment status enumeration (for payment registers)
-- Note: Based on Confluence design, this enum has values: DRAFT, APPROVED, ISSUED, VOIDED
CREATE TYPE IF NOT EXISTS ap_payment_status AS ENUM (
    'DRAFT',
    'APPROVED',
    'ISSUED',
    'VOIDED'
);

-- Assignment method enumeration (for payment run invoices)
CREATE TYPE IF NOT EXISTS ap_assignment_method AS ENUM (
    'AUTO',
    'MANUAL'
);

-- Job type enumeration (for payment run jobs)
CREATE TYPE IF NOT EXISTS ap_job_type AS ENUM (
    'PROPOSAL_GENERATION',
    'PAYMENT_CREATION',
    'PAYMENT_ISSUE',
    'PAYMENT_VOID'
);

-- Job status enumeration (for payment run jobs)
CREATE TYPE IF NOT EXISTS ap_job_status AS ENUM (
    'STARTED',
    'COMPLETED',
    'FAILED',
    'STOPPED'
);

-- ============================================
-- TABLES
-- ============================================
-- Tables are defined in dependency order to ensure foreign key constraints
-- can be properly established.

-- ============================================
-- AP_BANK_ACCOUNT
-- ============================================
-- Table for managing bank accounts used for payments.
-- Supports multitenancy with entity_id field.
--
-- Key Features:
--   - Multitenant support (entity_id required)
--   - Stores bank account details for payment processing

DROP TABLE IF EXISTS ap_bank_account CASCADE;

CREATE TABLE ap_bank_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    account_name VARCHAR(200),
    account_number VARCHAR(50),
    routing_number VARCHAR(50),
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_bank_account_entity 
    ON ap_bank_account(entity_id);

-- ============================================
-- AP_REMITTANCE_MESSAGE_TEMPLATE
-- ============================================
-- Table for managing remittance message templates.
-- Supports multitenancy with entity_id field.
--
-- Key Features:
--   - Multitenant support (entity_id required)
--   - Stores template body for remittance messages

DROP TABLE IF EXISTS ap_remittance_message_template CASCADE;

CREATE TABLE ap_remittance_message_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    template_body TEXT NOT NULL,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_remittance_template_entity 
    ON ap_remittance_message_template(entity_id);

-- ============================================
-- AP_PAYMENT_RUN
-- ============================================
-- Main table for payment runs. Represents a batch of vendor invoices
-- collected together for payment processing.
--
-- Key Features:
--   - Multitenant support (entity_id required)
--   - Unique run_number per entity
--   - Tracks approval workflow (submitted, approved, rejected)
--   - Links to Spring Batch job executions for processing
--   - Optimistic locking via row_version

DROP TABLE IF EXISTS ap_payment_run CASCADE;

CREATE TABLE ap_payment_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    run_number VARCHAR(20) NOT NULL,
    run_description VARCHAR(200),
    run_type ap_payment_run_type NOT NULL,
    payment_method ap_payment_method NOT NULL,
    bank_account_id UUID NOT NULL,
    payment_date DATE NOT NULL,
    remittance_message_template_id UUID,
    status ap_payment_run_status NOT NULL DEFAULT 'DRAFT',
    -- Spring Batch job execution IDs (references BATCH_JOB_EXECUTION.JOB_EXECUTION_ID)
    prepare_job_execution_id BIGINT,
    process_job_execution_id BIGINT,
    submitted_at TIMESTAMPTZ,
    submitted_by UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,
    issued_at TIMESTAMPTZ,
    issued_by UUID,
    rejected_at TIMESTAMPTZ,
    rejected_by UUID,
    rejection_reason VARCHAR(200),
    cancelled_at TIMESTAMPTZ,
    cancelled_by UUID,
    cancel_reason VARCHAR(200),
    completed_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    row_version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_payment_run_bank_account
        FOREIGN KEY (bank_account_id)
        REFERENCES ap_bank_account(id),
    CONSTRAINT fk_payment_run_template
        FOREIGN KEY (remittance_message_template_id)
        REFERENCES ap_remittance_message_template(id),
    CONSTRAINT uk_payment_run_entity_run_number 
        UNIQUE (entity_id, run_number)
);

CREATE INDEX IF NOT EXISTS idx_pay_payment_run_status 
    ON ap_payment_run(status);

CREATE INDEX IF NOT EXISTS idx_pay_payment_run_payment_date 
    ON ap_payment_run(payment_date);

CREATE INDEX IF NOT EXISTS idx_pay_payment_run_bank_account 
    ON ap_payment_run(bank_account_id);

-- Composite index for common query patterns (entity + status)
-- Note: This index covers entity_id queries, so idx_pay_payment_run_entity is redundant
-- Note: Unique constraint uk_payment_run_entity_run_number already creates index on (entity_id, run_number)
CREATE INDEX IF NOT EXISTS idx_pay_payment_run_entity_status 
    ON ap_payment_run(entity_id, status);

-- Partial index for active payment runs (performance optimization)
CREATE INDEX IF NOT EXISTS idx_pay_payment_run_active
    ON ap_payment_run(entity_id, status)
    WHERE status NOT IN ('COMPLETED', 'CANCELLED', 'FAILED');

-- ============================================
-- AP_PAYMENT_RUN_INVOICE
-- ============================================
-- Relationship table linking payment runs to invoices.
-- Tracks which invoices are assigned to a payment run and whether
-- they are included or excluded from payment processing.
--
-- Key Features:
--   - Links payment runs to invoices
--   - Tracks assignment method (AUTO or MANUAL)
--   - Supports inclusion/exclusion of invoices
--   - Tracks proposal version for versioning support

DROP TABLE IF EXISTS ap_payment_run_invoice CASCADE;

CREATE TABLE ap_payment_run_invoice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    payment_run_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    assignment_method ap_assignment_method NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_by UUID,
    included BOOLEAN NOT NULL DEFAULT true,
    exclusion_reason TEXT,
    proposal_version INT,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_run_invoice_payment_run
        FOREIGN KEY (payment_run_id)
        REFERENCES ap_payment_run(id) ON DELETE CASCADE,
    CONSTRAINT uq_ap_run_invoice_assignment
        UNIQUE (payment_run_id, invoice_id),
    CONSTRAINT chk_manual_assignment_user
        CHECK (
            assignment_method = 'AUTO'
            OR (assignment_method = 'MANUAL' AND assigned_by IS NOT NULL)
        ),
    CONSTRAINT chk_exclusion_reason
        CHECK (
            included = true
            OR (included = false AND exclusion_reason IS NOT NULL)
        )
);

CREATE INDEX IF NOT EXISTS ix_ap_run_invoice_assignment_run
    ON ap_payment_run_invoice(payment_run_id);

CREATE INDEX IF NOT EXISTS ix_ap_run_invoice_assignment_invoice
    ON ap_payment_run_invoice(invoice_id);

CREATE INDEX IF NOT EXISTS ix_ap_run_invoice_assignment_entity
    ON ap_payment_run_invoice(entity_id);

-- Composite index for filtering included invoices by entity
CREATE INDEX IF NOT EXISTS ix_ap_run_invoice_entity_included
    ON ap_payment_run_invoice(entity_id, included)
    WHERE included = true;

-- ============================================
-- AP_PAYMENT_REGISTER
-- ============================================
-- Payment register table. Represents a single vendor payment within a payment run.
-- One record per vendor per payment run.
--
-- Key Features:
--   - Links to payment run (cascade delete)
--   - Stores vendor payment details
--   - Tracks payment number (unique per entity)
--   - Supports versioning via proposal_version
--   - Tracks payment status and lifecycle events

DROP TABLE IF EXISTS ap_payment_register CASCADE;

CREATE TABLE ap_payment_register (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    payment_run_id UUID NOT NULL,
    payment_register_id VARCHAR(50),
    payment_id VARCHAR(50),
    proposal_version INT NOT NULL,
    superseded_at TIMESTAMPTZ,
    payment_number VARCHAR(20),
    payment_method ap_payment_method NOT NULL,
    payment_date DATE NOT NULL,
    bank_account_id UUID NOT NULL,
    vendor_id UUID NOT NULL,
    vendor_name VARCHAR(200) NOT NULL,
    vendor_number VARCHAR(50),
    remit_address_snapshot TEXT,
    payment_amount DECIMAL(18,4) NOT NULL,
    discount_taken DECIMAL(18,4) NOT NULL DEFAULT 0,
    interest_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
    net_amount DECIMAL(18,4) NOT NULL,
    payment_status ap_payment_status NOT NULL DEFAULT 'DRAFT',
    issued_at TIMESTAMPTZ,
    issued_by UUID,
    voided_at TIMESTAMPTZ,
    voided_by UUID,
    void_reason TEXT,
    row_version BIGINT NOT NULL DEFAULT 1,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_register_payment_run
        FOREIGN KEY (payment_run_id)
        REFERENCES ap_payment_run(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_register_bank_account
        FOREIGN KEY (bank_account_id)
        REFERENCES ap_bank_account(id),
    CONSTRAINT chk_payment_amount_positive 
        CHECK (payment_amount > 0),
    CONSTRAINT chk_net_amount_calculation 
        CHECK (net_amount = payment_amount - discount_taken + interest_amount)
);

CREATE INDEX IF NOT EXISTS idx_ap_payment_register_entity_id
    ON ap_payment_register(entity_id);

CREATE INDEX IF NOT EXISTS idx_ap_payment_register_payment_run_id
    ON ap_payment_register(payment_run_id);

CREATE INDEX IF NOT EXISTS idx_ap_payment_register_vendor_id
    ON ap_payment_register(vendor_id);

CREATE INDEX IF NOT EXISTS idx_ap_payment_register_status
    ON ap_payment_register(payment_status);

-- Entity-scoped uniqueness for payment_number (unique per entity, not globally)
CREATE UNIQUE INDEX IF NOT EXISTS ux_ap_payment_register_entity_payment_number
    ON ap_payment_register(entity_id, payment_number)
    WHERE payment_number IS NOT NULL;

-- Composite index for common query patterns (entity + status)
CREATE INDEX IF NOT EXISTS idx_ap_payment_register_entity_status
    ON ap_payment_register(entity_id, payment_status);

CREATE INDEX IF NOT EXISTS idx_ap_payment_register_payment_register_id
    ON ap_payment_register(payment_register_id)
    WHERE payment_register_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_ap_payment_register_payment_id
    ON ap_payment_register(payment_id)
    WHERE payment_id IS NOT NULL;

-- ============================================
-- AP_PAYMENT_REGISTER_LINE
-- ============================================
-- Payment line table. Represents the allocation of a payment amount to a specific invoice.
-- Links payment register to invoices.
--
-- Key Features:
--   - Links to payment register (cascade delete)
--   - Stores invoice allocation details
--   - Tracks discount amounts
--   - Calculates net amount (invoice_amount - discount_amount)

DROP TABLE IF EXISTS ap_payment_register_line CASCADE;

CREATE TABLE ap_payment_register_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    payment_register_id UUID NOT NULL,
    payment_id VARCHAR(50),
    invoice_id UUID,
    invoice_number VARCHAR(255) NOT NULL,
    invoice_amount DECIMAL(18,4) NOT NULL,
    discount_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
    net_amount DECIMAL(18,4) NOT NULL,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_line_payment_register
        FOREIGN KEY (payment_register_id)
        REFERENCES ap_payment_register(id) ON DELETE CASCADE,
    -- Note: Foreign key constraint for invoice_id is not added as it may reference
    -- an external invoice table. Add FK constraint when invoice table is available:
    -- CONSTRAINT fk_payment_line_invoice
    --     FOREIGN KEY (invoice_id)
    --     REFERENCES ap_invoice(id),
    CONSTRAINT chk_invoice_amount_positive 
        CHECK (invoice_amount > 0),
    CONSTRAINT chk_discount_amount_valid 
        CHECK (discount_amount >= 0 AND discount_amount <= invoice_amount),
    CONSTRAINT chk_net_amount_valid 
        CHECK (net_amount = invoice_amount - discount_amount)
);

CREATE INDEX IF NOT EXISTS idx_pay_payment_line_entity 
    ON ap_payment_register_line(entity_id);

CREATE INDEX IF NOT EXISTS idx_pay_payment_line_payment_register 
    ON ap_payment_register_line(payment_register_id);

CREATE INDEX IF NOT EXISTS idx_pay_payment_line_invoice 
    ON ap_payment_register_line(invoice_id);

CREATE INDEX IF NOT EXISTS idx_pay_payment_line_payment_id
    ON ap_payment_register_line(payment_id)
    WHERE payment_id IS NOT NULL;

-- ============================================
-- AP_PAYMENT_RUN_JOB
-- ============================================
-- Tracks Spring Batch job executions for payment run processing.
-- Links payment runs to Spring Batch job execution IDs.
--
-- Key Features:
--   - Links to payment run
--   - Tracks job type (PROPOSAL_GENERATION, PAYMENT_CREATION, etc.)
--   - Stores Spring Batch job execution ID
--   - Tracks job status and execution details

DROP TABLE IF EXISTS ap_payment_run_job CASCADE;

CREATE TABLE ap_payment_run_job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    payment_run_id UUID NOT NULL,
    job_type ap_job_type NOT NULL,
    -- Spring Batch job execution ID (references BATCH_JOB_EXECUTION.JOB_EXECUTION_ID)
    job_execution_id BIGINT NOT NULL,
    status ap_job_status NOT NULL DEFAULT 'STARTED',
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ended_at TIMESTAMPTZ,
    exit_code VARCHAR(50),
    exit_message TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_run_job_payment_run
        FOREIGN KEY (payment_run_id)
        REFERENCES ap_payment_run(id) ON DELETE CASCADE,
    CONSTRAINT chk_job_time
        CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE INDEX IF NOT EXISTS ix_ap_payment_run_job_entity
    ON ap_payment_run_job(entity_id);

CREATE INDEX IF NOT EXISTS ix_ap_payment_run_job_run
    ON ap_payment_run_job(payment_run_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ap_payment_run_job_execution
    ON ap_payment_run_job(job_execution_id);

CREATE INDEX IF NOT EXISTS ix_ap_payment_run_job_status
    ON ap_payment_run_job(status);

-- ============================================
-- AP_PAYMENT_RUN_TRANSITION
-- ============================================
-- Tracks payment run status transitions for audit and history.
-- Records all state changes in the payment run lifecycle.
--
-- Key Features:
--   - Links to payment run
--   - Tracks status transitions (from_status -> to_status)
--   - Records transition events and notes
--   - Links to job executions when applicable

DROP TABLE IF EXISTS ap_payment_run_transition CASCADE;

CREATE TABLE ap_payment_run_transition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL,
    payment_run_id UUID NOT NULL,
    from_status ap_payment_run_status NOT NULL,
    to_status ap_payment_run_status NOT NULL,
    event VARCHAR(50),
    notes TEXT,
    job_type ap_job_type,
    -- Spring Batch job execution ID (references BATCH_JOB_EXECUTION.JOB_EXECUTION_ID)
    job_execution_id BIGINT,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_run_transition_payment_run
        FOREIGN KEY (payment_run_id)
        REFERENCES ap_payment_run(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_ap_payment_run_transition_entity
    ON ap_payment_run_transition(entity_id);

CREATE INDEX IF NOT EXISTS ix_ap_payment_run_transition_run
    ON ap_payment_run_transition(payment_run_id);

CREATE INDEX IF NOT EXISTS ix_ap_payment_run_transition_job
    ON ap_payment_run_transition(job_execution_id)
    WHERE job_execution_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_ap_payment_run_transition_job_type
    ON ap_payment_run_transition(job_type)
    WHERE job_type IS NOT NULL;

-- ============================================
-- TRIGGERS
-- ============================================
-- Auto-update updated_at column on row updates

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_ap_bank_account_updated_at
    BEFORE UPDATE ON ap_bank_account
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ap_remittance_message_template_updated_at
    BEFORE UPDATE ON ap_remittance_message_template
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ap_payment_run_updated_at
    BEFORE UPDATE ON ap_payment_run
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ap_payment_run_invoice_updated_at
    BEFORE UPDATE ON ap_payment_run_invoice
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ap_payment_register_updated_at
    BEFORE UPDATE ON ap_payment_register
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ap_payment_register_line_updated_at
    BEFORE UPDATE ON ap_payment_register_line
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ap_payment_run_job_updated_at
    BEFORE UPDATE ON ap_payment_run_job
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ap_payment_run_transition_updated_at
    BEFORE UPDATE ON ap_payment_run_transition
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- END OF FILE
-- ============================================
-- 
-- Note: Sample data is available in a separate file:
--   docs/payment-run-sample-data.sql
--