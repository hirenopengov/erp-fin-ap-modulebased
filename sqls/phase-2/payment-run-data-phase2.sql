
-- ============================================================
-- Payment Run - Phase 2 Sample Data
-- Tables:
--   pr_ap_invoice_header
--   pr_ap_payment_run_workflow
--   pr_ap_payable
--   pr_ap_payable_run_item
-- ============================================================

-- ============================================================
-- TRUNCATE (respect FK order - child tables first)
-- ============================================================

TRUNCATE TABLE pr_ap_payment_register CASCADE;
TRUNCATE TABLE pr_ap_payment_artifact CASCADE;
TRUNCATE TABLE pr_ap_payable_run_item CASCADE;
TRUNCATE TABLE pr_ap_payable CASCADE;
TRUNCATE TABLE pr_ap_payment_run_workflow CASCADE;
TRUNCATE TABLE pr_ap_invoice_header CASCADE;

-- ============================================================
-- SAMPLE CONSTANT IDS
-- ============================================================
-- Entity
-- 11111111-1111-1111-1111-111111111111

-- Invoices
-- 22222222-2222-2222-2222-222222222221
-- 22222222-2222-2222-2222-222222222222

-- Payables
-- 33333333-3333-3333-3333-333333333331
-- 33333333-3333-3333-3333-333333333332

-- Payment Run
-- 44444444-4444-4444-4444-444444444441

-- Run Items
-- 55555555-5555-5555-5555-555555555551
-- 55555555-5555-5555-5555-555555555552


-- ============================================================
-- INSERT: pr_ap_invoice_header
-- ============================================================

INSERT INTO pr_ap_invoice_header (
    id, entity_id, invoice_header_id, ap_vendor_id,
    currency, invoice_amount, invoice_due_date,
    invoice_type, invoice_status, invoice_source,
    has_errors, created_by, created_at, updated_by, updated_at
)
VALUES
(
    '22222222-2222-2222-2222-222222222221',
    '11111111-1111-1111-1111-111111111111',
    10001,
    5001,
    'USD',
    1000.00,
    CURRENT_DATE + INTERVAL '15 days',
    'STANDARD',
    'APPROVED',
    'MANUAL',
    false,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now()
),
(
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    10002,
    5002,
    'USD',
    2500.00,
    CURRENT_DATE + INTERVAL '20 days',
    'STANDARD',
    'APPROVED',
    'IMPORT',
    false,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now()
);


-- ============================================================
-- INSERT: pr_ap_payment_run_workflow
-- ============================================================

INSERT INTO pr_ap_payment_run_workflow (
    id, entity_id, run_number, run_description,
    status, config_snapshot,
    created_by, created_at, updated_by, updated_at
)
VALUES (
    '44444444-4444-4444-4444-444444444441',
    '11111111-1111-1111-1111-111111111111',
    'PR-2025-0001',
    'Phase 2 Test Run',
    'READY_FOR_REVIEW',
    '{}'::jsonb,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now()
);


-- ============================================================
-- INSERT: pr_ap_payable
-- ============================================================

INSERT INTO pr_ap_payable (
    id, entity_id, invoice_id,
    payable_type, currency,
    original_amount, open_amount,
    due_date, status,
    created_by, created_at, updated_by, updated_at
)
VALUES
(
    '33333333-3333-3333-3333-333333333331',
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222221',
    'INVOICE',
    'USD',
    1000.00,
    1000.00,
    CURRENT_DATE + INTERVAL '15 days',
    'OPEN',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now()
),
(
    '33333333-3333-3333-3333-333333333332',
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    'INVOICE',
    'USD',
    2500.00,
    2500.00,
    CURRENT_DATE + INTERVAL '20 days',
    'OPEN',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now()
);


-- ============================================================
-- INSERT: pr_ap_payable_run_item
-- ============================================================

INSERT INTO pr_ap_payable_run_item (
    id, entity_id, payment_run_id, payable_id,
    status, include,
    computed_amount_to_pay,
    computed_discount_taken,
    computed_payment_method,
    computed_payment_date,
    created_by, created_at, updated_by, updated_at
)
VALUES
(
    '55555555-5555-5555-5555-555555555551',
    '11111111-1111-1111-1111-111111111111',
    '44444444-4444-4444-4444-444444444441',
    '33333333-3333-3333-3333-333333333331',
    'INCLUDED',
    true,
    1000.00,
    0.00,
    'ACH',
    CURRENT_DATE + INTERVAL '5 days',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now()
),
(
    '55555555-5555-5555-5555-555555555552',
    '11111111-1111-1111-1111-111111111111',
    '44444444-4444-4444-4444-444444444441',
    '33333333-3333-3333-3333-333333333332',
    'NEEDS_REVIEW',
    true,
    2500.00,
    0.00,
    'CHECK',
    CURRENT_DATE + INTERVAL '5 days',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    now()
);
