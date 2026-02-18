
-- ============================================================
-- AP Schema (PostgreSQL): Enums + Tables + Indexes + FKs
-- ============================================================

-- ============================================================
-- ENUM TYPES
-- ============================================================

CREATE TYPE ap_payable_status AS ENUM ('OPEN', 'CLOSED', 'VOID');

CREATE TYPE ap_payable_run_item_status AS ENUM (
  'DRAFT', 'INCLUDED', 'EXCLUDED', 'NEEDS_REVIEW', 'LOCKED', 'EXECUTED', 'SKIPPED'
);

CREATE TYPE ap_payment_run_status AS ENUM (
  'DRAFT', 'SUBMITTED', 'READY_FOR_REVIEW', 'PENDING_APPROVAL', 'APPROVED',
  'ARTIFACT_PENDING', 'ARTIFACT_READY', 'COMPLETED', 'REJECTED', 'FAILED', 'CANCELLED'
);

CREATE TYPE ap_payment_method AS ENUM ('CHECK', 'ACH', 'WIRE', 'WARRANT');

CREATE TYPE ap_payment_artifact_type AS ENUM ('CHECK', 'ACH', 'WIRE', 'POSITIVE_PAY', 'REMITTANCE');

CREATE TYPE ap_payment_artifact_status AS ENUM (
  'CREATED', 'GENERATED', 'TRANSMITTED', 'ACCEPTED', 'REJECTED', 'REISSUED', 'FAILED'
);

CREATE TYPE ap_payment_register_status AS ENUM ('ISSUED', 'VOIDED', 'REVERSED', 'SETTLED', 'FAILED');

CREATE TYPE invoice_source AS ENUM ('MANUAL', 'IMPORT', 'INTEGRATION', 'OTHER');

-- ============================================================
-- TABLES
-- ============================================================

DROP TABLE IF EXISTS pr_ap_invoice_header;
CREATE TABLE pr_ap_invoice_header (
  id uuid PRIMARY KEY NOT NULL,
  entity_id uuid NOT NULL,

  invoice_header_id bigint NOT NULL UNIQUE,
  ap_vendor_id int NOT NULL,
  currency varchar(8) NOT NULL,
  invoice_amount numeric(18,4) NOT NULL,

  invoice_due_date date,
  due_date_override date,

  invoice_type varchar(64) NOT NULL,
  invoice_status varchar(64) NOT NULL,
  invoice_source invoice_source NOT NULL,

  has_errors boolean NOT NULL DEFAULT false,

  created_by uuid NOT NULL,
  created_at timestamptz NOT NULL,
  updated_by uuid NOT NULL,
  updated_at timestamptz NOT NULL
);

CREATE INDEX ix_ap_invoice_vendor
  ON pr_ap_invoice_header (entity_id, ap_vendor_id);

CREATE INDEX ix_ap_invoice_status
  ON pr_ap_invoice_header (entity_id, invoice_status);

DROP TABLE IF EXISTS pr_ap_payable;
CREATE TABLE pr_ap_payable (
  id uuid PRIMARY KEY NOT NULL,
  entity_id uuid NOT NULL,

  invoice_id uuid NOT NULL,

  payable_type varchar(64) NOT NULL DEFAULT 'INVOICE',
  payable_reference varchar(128),

  currency varchar(8) NOT NULL,

  original_amount numeric(18,4) NOT NULL,
  open_amount numeric(18,4) NOT NULL,

  due_date date,
  discount_deadline date,
  discount_max numeric(18,4),

  is_on_hold boolean NOT NULL DEFAULT false,
  hold_code varchar(64),
  hold_reason text,

  status ap_payable_status NOT NULL DEFAULT 'OPEN',

  row_version bigint NOT NULL DEFAULT 1,
  created_by uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_by uuid NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_ap_payable_invoice
    FOREIGN KEY (invoice_id) REFERENCES pr_ap_invoice_header(id)
);

CREATE INDEX ix_ap_payable_invoice
  ON pr_ap_payable (invoice_id);

CREATE INDEX ix_ap_payable_entity_status_due
  ON pr_ap_payable (entity_id, status, due_date);

CREATE INDEX ix_ap_payable_invoice_type
  ON pr_ap_payable (entity_id, invoice_id, payable_type);

DROP TABLE IF EXISTS pr_ap_payment_run_workflow;
CREATE TABLE pr_ap_payment_run_workflow (
  id uuid PRIMARY KEY NOT NULL,
  entity_id uuid NOT NULL,

  run_number varchar(20) NOT NULL,
  run_description varchar(200),

  status ap_payment_run_status NOT NULL DEFAULT 'DRAFT',

  config_snapshot jsonb NOT NULL DEFAULT '{}'::jsonb,

  prepare_job_execution_id bigint,
  artifact_job_execution_id bigint,
  send_job_execution_id bigint,

  submitted_at timestamptz,
  submitted_by uuid,

  reviewed_at timestamptz,
  reviewed_by uuid,

  approved_at timestamptz,
  approved_by uuid,

  issued_at timestamptz,
  issued_by uuid,

  cancelled_at timestamptz,
  cancelled_by uuid,
  cancel_reason text,

  row_version bigint NOT NULL DEFAULT 1,
  created_by uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_by uuid NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT uq_ap_payment_run_number
    UNIQUE (entity_id, run_number)
);

CREATE INDEX ix_ap_payment_run_status
  ON pr_ap_payment_run_workflow (entity_id, status);

DROP TABLE IF EXISTS pr_ap_payable_run_item;
CREATE TABLE pr_ap_payable_run_item (
  id uuid PRIMARY KEY NOT NULL,
  entity_id uuid NOT NULL,

  payment_run_id uuid NOT NULL,
  payable_id uuid NOT NULL,

  status ap_payable_run_item_status NOT NULL DEFAULT 'DRAFT',
  include boolean NOT NULL DEFAULT true,

  computed_amount_to_pay numeric(18,4),
  computed_discount_taken numeric(18,4),
  computed_payment_method ap_payment_method,
  computed_bank_account_id uuid,
  computed_payment_date date,

  requested_amount_to_pay numeric(18,4),
  requested_discount_taken numeric(18,4),
  requested_payment_method ap_payment_method,
  requested_bank_account_id uuid,
  requested_payment_date date,

  exception_code varchar(64),
  exception_detail text,

  assigned_at timestamptz,
  assigned_by uuid,

  row_version bigint NOT NULL DEFAULT 1,
  created_by uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_by uuid NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_ap_payable_run_item_run
    FOREIGN KEY (payment_run_id) REFERENCES pr_ap_payment_run_workflow(id),

  CONSTRAINT fk_ap_payable_run_item_payable
    FOREIGN KEY (payable_id) REFERENCES pr_ap_payable(id),

  CONSTRAINT uq_ap_payable_once_per_run
    UNIQUE (payment_run_id, payable_id)
);

CREATE INDEX ix_ap_payable_run_item_run_status
  ON pr_ap_payable_run_item (payment_run_id, status);

CREATE INDEX ix_ap_payable_run_item_payable
  ON pr_ap_payable_run_item (payable_id);

DROP TABLE IF EXISTS pr_ap_payment_artifact;
CREATE TABLE pr_ap_payment_artifact (
  id uuid PRIMARY KEY NOT NULL,
  entity_id uuid NOT NULL,

  payment_run_id uuid NOT NULL,

  artifact_type ap_payment_artifact_type NOT NULL,
  format varchar(32),

  status ap_payment_artifact_status NOT NULL DEFAULT 'CREATED',

  content_ref text,
  content_hash varchar(128),

  transmission_ref text,
  transmission_metadata jsonb,

  idempotency_key varchar(128) NOT NULL,

  created_by uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_by uuid NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_ap_payment_artifact_run
    FOREIGN KEY (payment_run_id) REFERENCES pr_ap_payment_run_workflow(id),

  CONSTRAINT uq_ap_payment_artifact_idempotency
    UNIQUE (entity_id, idempotency_key)
);

CREATE INDEX ix_ap_payment_artifact_run
  ON pr_ap_payment_artifact (payment_run_id);

CREATE INDEX ix_ap_payment_artifact_type_status
  ON pr_ap_payment_artifact (entity_id, artifact_type, status);

DROP TABLE IF EXISTS pr_ap_payment_register;
CREATE TABLE pr_ap_payment_register (
  id uuid PRIMARY KEY NOT NULL,
  entity_id uuid NOT NULL,

  payment_run_id uuid NOT NULL,
  artifact_id uuid,

  payment_method ap_payment_method NOT NULL,
  bank_account_id uuid NOT NULL,

  ap_vendor_id int NOT NULL,
  payee_name varchar(255),

  currency varchar(8) NOT NULL,
  payment_date date NOT NULL,
  issued_at timestamptz,

  gross_amount numeric(18,4) NOT NULL,
  net_amount numeric(18,4) NOT NULL,
  discount_taken numeric(18,4) NOT NULL DEFAULT 0,
  withholding_amount numeric(18,4) NOT NULL DEFAULT 0,

  check_number varchar(64),
  ach_trace_number varchar(64),
  wire_reference varchar(128),

  status ap_payment_register_status NOT NULL DEFAULT 'ISSUED',

  payable_refs jsonb,
  external_reference varchar(128),

  row_version bigint NOT NULL DEFAULT 1,
  created_by uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_by uuid NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_ap_payment_register_run
    FOREIGN KEY (payment_run_id) REFERENCES pr_ap_payment_run_workflow(id),

  CONSTRAINT fk_ap_payment_register_artifact
    FOREIGN KEY (artifact_id) REFERENCES pr_ap_payment_artifact(id)
);

CREATE INDEX ix_ap_payment_register_date
  ON pr_ap_payment_register (entity_id, payment_date);

CREATE INDEX ix_ap_payment_register_status
  ON pr_ap_payment_register (entity_id, status);

CREATE INDEX ix_ap_payment_register_method_date
  ON pr_ap_payment_register (entity_id, payment_method, payment_date);

CREATE INDEX ix_ap_payment_register_bank_date
  ON pr_ap_payment_register (entity_id, bank_account_id, payment_date);
