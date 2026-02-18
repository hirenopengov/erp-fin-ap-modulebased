-- ============================================
-- PAYMENT RUN SAMPLE DATA
-- ============================================
-- Sample INSERT statements for testing and development.
-- 
-- Prerequisites:
--   - Run payment-run-database-design.sql first to create the schema
--   - All UUIDs are example values - replace with actual UUIDs in production
--   - All user UUIDs (created_by, updated_by, etc.) should reference actual user records
--
-- Usage:
--   psql -d your_database -f payment-run-sample-data.sql
--
-- ============================================

-- ============================================
-- AP_BANK_ACCOUNT Sample Data
-- ============================================

INSERT INTO ap_bank_account (id, entity_id, account_name, account_number, routing_number, created_by, updated_by, created_at, updated_at)
VALUES 
    ('1', 'ENT001', 'Operating Account', '1234567890', '021000021', '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', now(), now()),
    ('2', 'ENT001', 'Payroll Account', '0987654321', '021000021', '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', now(), now()),
    ('3', 'ENT001', 'Main Checking', '5555555555', '026009593', '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- AP_REMITTANCE_MESSAGE_TEMPLATE Sample Data
-- ============================================

INSERT INTO ap_remittance_message_template (id, entity_id, template_name, template_body, created_by, updated_by, created_at, updated_at)
VALUES 
    ('1', 'ENT001', 'Standard Remittance', 'Payment for invoices: {invoice_numbers}. Thank you for your business.', '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', now(), now()),
    ('2', 'ENT001', 'Detailed Remittance', 'This payment covers the following invoices: {invoice_list}. Payment date: {payment_date}.', '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', now(), now())
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- AP_PAYMENT_RUN Sample Data
-- ============================================

INSERT INTO ap_payment_run (id, entity_id, run_number, run_description, run_type, payment_method, bank_account_id, payment_date, remittance_message_template_id, status, prepare_job_execution_id, process_job_execution_id, submitted_at, submitted_by, approved_at, approved_by, created_by, updated_by, created_at, updated_at, row_version)
VALUES 
    ('1', 'ENT001', 'PR-2024-001', 'Weekly payment run for vendors', 'REGULAR', 'ACH', '1', '2024-02-15', '1', 'APPROVED', 1001, 1002, '2024-02-14 10:00:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 14:30:00+00', '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 09:00:00+00', '2024-02-14 14:30:00+00', 1)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- AP_PAYMENT_RUN_INVOICE Sample Data
-- ============================================

INSERT INTO ap_payment_run_invoice (id, entity_id, payment_run_id, invoice_id, assignment_method, assigned_at, assigned_by, included, exclusion_reason, proposal_version, created_by, updated_by, created_at, updated_at)
VALUES 
    ('1', 'ENT001', '1', '1', 'AUTO', '2024-02-14 09:15:00+00', NULL, true, NULL, 1, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 09:15:00+00', '2024-02-14 09:15:00+00'),
    ('2', 'ENT001', '1', '2', 'AUTO', '2024-02-14 09:15:00+00', NULL, true, NULL, 1, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 09:15:00+00', '2024-02-14 09:15:00+00'),
    ('3', 'ENT001', '1', '3', 'MANUAL', '2024-02-14 09:20:00+00', '200e8400-e29b-41d4-a716-446655440000', true, NULL, 1, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 09:20:00+00', '2024-02-14 09:20:00+00'),
    ('4', 'ENT001', '1', '4', 'AUTO', '2024-02-14 09:15:00+00', NULL, false, 'Invoice on hold pending approval', 1, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 09:15:00+00', '2024-02-14 09:15:00+00')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- AP_PAYMENT_REGISTER Sample Data
-- Note: payment_id groups vendor payments, payment_register_id groups payment registers
-- Each payment register represents one vendor payment within a payment run
-- ============================================

INSERT INTO ap_payment_register (id, entity_id, payment_run_id, payment_register_id, payment_id, proposal_version, superseded_at, payment_number, payment_method, payment_date, bank_account_id, vendor_id, vendor_name, vendor_number, remit_address_snapshot, payment_amount, discount_taken, interest_amount, net_amount, payment_status, issued_at, issued_by, voided_at, voided_by, void_reason, row_version, created_by, created_at, updated_by, updated_at)
VALUES 
    ('1', 'ENT001', '1','PRID1', 'PYID1', 1, NULL, 'PAY-2024-00001', 'ACH', '2024-02-15', '1', '1', 'ABC Supplies Inc.', 'VEND-001', '123 Main St, City, State 12345', 15000.0000, 150.0000, 0.0000, 14850.0000, 'ISSUED', '2024-02-15 10:00:00+00', '200e8400-e29b-41d4-a716-446655440000', NULL, NULL, NULL, 1, '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 15:00:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-15 10:00:00+00'),
    ('2', 'ENT001', '1','PRID2', 'PYID2', 1, NULL, 'PAY-2024-00002', 'ACH', '2024-02-15', '1', '2', 'XYZ Corporation', 'VEND-002', '456 Oak Ave, City, State 67890', 15000.0000, 150.0000, 0.0000, 14850.0000, 'ISSUED', '2024-02-15 10:00:00+00', '200e8400-e29b-41d4-a716-446655440000', NULL, NULL, NULL, 1, '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 15:00:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-15 10:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- AP_PAYMENT_REGISTER_LINE Sample Data
-- Note: payment_register_id references ap_payment_register.id (UUID)
-- Each line represents an invoice allocation within a payment register
-- ============================================

INSERT INTO ap_payment_register_line (id, entity_id, payment_register_id, payment_id, invoice_id, invoice_number, invoice_amount, discount_amount, net_amount, created_by, updated_by, created_at, updated_at)
VALUES 
    -- Lines for payment register '1' (PYID1)
    ('1', 'ENT001', '1', 'PYID1', '1', 'INV-2024-001', 10000.0000, 100.0000, 9900.0000, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 15:30:00+00', '2024-02-14 15:30:00+00'),
    ('2', 'ENT001', '1', 'PYID1', '2', 'INV-2024-002', 5000.0000, 50.0000, 4950.0000, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 15:30:00+00', '2024-02-14 15:30:00+00'),
    -- Lines for payment register '2' (PYID2)
    ('3', 'ENT001', '2', 'PYID2', '3', 'INV-2024-003', 10000.0000, 100.0000, 9900.0000, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 15:30:00+00', '2024-02-14 15:30:00+00'),
    ('4', 'ENT001', '2', 'PYID2', '4', 'INV-2024-004', 5000.0000, 50.0000, 4950.0000, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 15:30:00+00', '2024-02-14 15:30:00+00')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- AP_PAYMENT_RUN_JOB Sample Data
-- ============================================

INSERT INTO ap_payment_run_job (id, entity_id, payment_run_id, job_type, job_execution_id, status, started_at, ended_at, exit_code, exit_message, created_by, created_at, updated_by, updated_at)
VALUES 
    ('1', 'ENT001', '1', 'PROPOSAL_GENERATION', 1001, 'COMPLETED', '2024-02-14 09:30:00+00', '2024-02-14 10:00:00+00', 'COMPLETED', 'Proposal generated successfully', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 09:30:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 10:00:00+00'),
    ('2', 'ENT001', '1', 'PAYMENT_CREATION', 1002, 'COMPLETED', '2024-02-14 14:30:00+00', '2024-02-14 15:00:00+00', 'COMPLETED', 'Payments created successfully', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 14:30:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 15:00:00+00'),
    ('3', 'ENT001', '3', 'PROPOSAL_GENERATION', 1003, 'COMPLETED', '2024-02-19 08:30:00+00', '2024-02-19 09:00:00+00', 'COMPLETED', 'Proposal generated successfully', '200e8400-e29b-41d4-a716-446655440000', '2024-02-19 08:30:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-19 09:00:00+00'),
    ('4', 'ENT001', '3', 'PAYMENT_ISSUE', 1004, 'STARTED', '2024-02-19 10:00:00+00', NULL, NULL, NULL, '200e8400-e29b-41d4-a716-446655440000', '2024-02-19 10:00:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-19 10:00:00+00'),
    ('5', 'ENT001', '2', 'PROPOSAL_GENERATION', 1005, 'FAILED', '2024-02-15 11:30:00+00', '2024-02-15 11:35:00+00', 'FAILED', 'Error: Insufficient funds in bank account', '200e8400-e29b-41d4-a716-446655440000', '2024-02-15 11:30:00+00', '200e8400-e29b-41d4-a716-446655440000', '2024-02-15 11:35:00+00')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- AP_PAYMENT_RUN_TRANSITION Sample Data
-- ============================================

INSERT INTO ap_payment_run_transition (id, entity_id, payment_run_id, from_status, to_status, event, notes, job_type, job_execution_id, created_by, updated_by, created_at, updated_at)
VALUES 
    ('1', 'ENT001', '1', 'DRAFT', 'SUBMITTED', 'SUBMIT', 'Payment run submitted for approval', NULL, NULL, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 10:00:00+00', '2024-02-14 10:00:00+00'),
    ('2', 'ENT001', '1', 'SUBMITTED', 'PREPARING', 'PREPARE', 'Starting proposal generation', 'PROPOSAL_GENERATION', 1001, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 09:30:00+00', '2024-02-14 09:30:00+00'),
    ('3', 'ENT001', '1', 'PREPARING', 'PENDING_APPROVAL', 'PROPOSAL_COMPLETE', 'Proposal generation completed', 'PROPOSAL_GENERATION', 1001, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 10:00:00+00', '2024-02-14 10:00:00+00'),
    ('4', 'ENT001', '1', 'PENDING_APPROVAL', 'APPROVED', 'APPROVE', 'Payment run approved by finance manager', NULL, NULL, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 14:30:00+00', '2024-02-14 14:30:00+00'),
    ('5', 'ENT001', '1', 'APPROVED', 'PROCESSING', 'PROCESS', 'Starting payment creation', 'PAYMENT_CREATION', 1002, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-14 14:30:00+00', '2024-02-14 14:30:00+00'),
    ('6', 'ENT001', '3', 'DRAFT', 'SUBMITTED', 'SUBMIT', 'Payment run submitted', NULL, NULL, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-19 09:00:00+00', '2024-02-19 09:00:00+00'),
    ('7', 'ENT001', '3', 'SUBMITTED', 'APPROVED', 'APPROVE', 'Approved and processing', NULL, NULL, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-19 10:00:00+00', '2024-02-19 10:00:00+00'),
    ('8', 'ENT001', '3', 'APPROVED', 'PROCESSING', 'PROCESS', 'Issuing payments', 'PAYMENT_ISSUE', 1004, '200e8400-e29b-41d4-a716-446655440000', '200e8400-e29b-41d4-a716-446655440000', '2024-02-19 10:00:00+00', '2024-02-19 10:00:00+00')
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- END OF FILE
-- ============================================
