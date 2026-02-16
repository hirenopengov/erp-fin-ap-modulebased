# Database Setup Guide

This application uses PostgreSQL as the database with multitenancy support using `entity_id` as the tenant identifier.

## Prerequisites

1. **PostgreSQL Installation**
   - Install PostgreSQL 12 or higher
   - Ensure PostgreSQL service is running

2. **Database Creation**
   - Create a database for the application

## Database Setup Steps

### 1. Create Database

Connect to PostgreSQL and create the database:

```sql
-- Connect to PostgreSQL as superuser
psql -U postgres

-- Create database
CREATE DATABASE fin_ap_batch;

-- Create user (optional, if not using default postgres user)
CREATE USER fin_ap_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE fin_ap_batch TO fin_ap_user;

-- Connect to the new database
\c fin_ap_batch
```

### 2. Configure Application

Update `application.yml` or set environment variables:

**Option 1: Update application.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fin_ap_batch
    username: postgres
    password: your_password
```

**Option 2: Use Environment Variables**
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

### 3. Spring Batch Metadata Tables

Spring Batch will automatically create its metadata tables on first run if:
```yaml
spring:
  batch:
    jdbc:
      initialize-schema: always
```

The following tables will be created automatically:
- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_JOB_EXECUTION_PARAMS`
- `BATCH_STEP_EXECUTION`
- `BATCH_STEP_EXECUTION_CONTEXT`
- `BATCH_JOB_EXECUTION_CONTEXT`

### 4. Application Tables

Application tables will be created automatically by Hibernate with `ddl-auto: update`:
- `employees` - Employee entity table with `entity_id` column

## Multitenancy Architecture

### Entity ID (Tenant ID)

Every table in the application includes an `entity_id` column that serves as the tenant identifier:

```sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    entity_id VARCHAR(50) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50),
    salary DECIMAL(10,2),
    status VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(employee_id, entity_id)
);
```

### Tenant Context

The application uses `TenantContext` (ThreadLocal) to manage tenant isolation:
- Tenant ID is set from job parameters (`entityId`)
- All queries automatically filter by `entity_id`
- Tenant context is cleared after job completion

## Running Jobs with Tenant ID

All jobs require an `entityId` parameter:

```bash
# Payment Processing Job
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0

# Payment Disbursement Job
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=7.5
```

## Database Schema Verification

After running the application, verify tables were created:

```sql
-- List all tables
\dt

-- Check Spring Batch tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' AND table_name LIKE 'BATCH_%';

-- Check application tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' AND table_name NOT LIKE 'BATCH_%';

-- Verify entity_id column exists
\d employees
```

## Troubleshooting

### Connection Issues

1. **Check PostgreSQL is running:**
   ```bash
   # Linux/Mac
   sudo systemctl status postgresql
   
   # Windows
   # Check Services panel
   ```

2. **Verify connection settings:**
   - Host: localhost
   - Port: 5432 (default)
   - Database: fin_ap_batch
   - Username/Password: as configured

3. **Test connection:**
   ```bash
   psql -h localhost -U postgres -d fin_ap_batch
   ```

### Schema Issues

1. **If tables are not created:**
   - Check `ddl-auto: update` is set in `application.yml`
   - Check database user has CREATE TABLE permissions
   - Review application logs for Hibernate errors

2. **If Spring Batch tables are missing:**
   - Ensure `spring.batch.jdbc.initialize-schema: always` is set
   - Check database user has CREATE TABLE permissions

### Multitenancy Issues

1. **"No tenant context set" error:**
   - Ensure `entityId` parameter is provided when running jobs
   - Check step listeners are setting tenant context

2. **Data from wrong tenant:**
   - Verify `entity_id` is being set correctly
   - Check repository queries include `entity_id` filter
   - Review `TenantContext` usage

## Production Considerations

1. **Connection Pooling:**
   - HikariCP is configured with:
     - Maximum pool size: 10
     - Minimum idle: 5
     - Connection timeout: 30 seconds

2. **Database Indexes:**
   - Consider adding indexes on `entity_id` columns for better performance:
   ```sql
   CREATE INDEX idx_employees_entity_id ON employees(entity_id);
   CREATE INDEX idx_employees_employee_id_entity_id ON employees(employee_id, entity_id);
   ```

3. **Schema Management:**
   - For production, consider using Flyway or Liquibase instead of `ddl-auto: update`
   - Review and optimize database schema

4. **Security:**
   - Use strong database passwords
   - Limit database user permissions
   - Use connection encryption (SSL) for production
