# Job Execution Guide

This guide explains how to execute Spring Batch jobs in this application.

## Prerequisites

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Ensure input CSV file exists:**
   - Place your CSV file at: `src/main/resources/data/input/employees.csv`
   - Or specify a custom path using the `inputFile` parameter

## Execution Methods

### Method 1: Using Maven (Development)

#### Run with default parameters:
```bash
# Payment Processing Job (entityId is required)
mvn spring-boot:run -Dspring-boot.run.arguments="run paymentProcessingJob entityId=TENANT001"

# Payment Disbursement Job (entityId is required)
mvn spring-boot:run -Dspring-boot.run.arguments="run paymentDisbursementJob entityId=TENANT001"
```

#### Run with custom parameters:
```bash
# Payment Processing with custom bonus percentage
mvn spring-boot:run -Dspring-boot.run.arguments="run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0"

# Payment Processing with all parameters
mvn spring-boot:run -Dspring-boot.run.arguments="run paymentProcessingJob entityId=TENANT001 bonusPercentage=20.0 inputFile=data/input/employees.csv outputFile=custom_output.csv"

# Payment Disbursement with custom tax rate
mvn spring-boot:run -Dspring-boot.run.arguments="run paymentDisbursementJob entityId=TENANT001 taxRate=7.5"

# Payment Disbursement with all parameters
mvn spring-boot:run -Dspring-boot.run.arguments="run paymentDisbursementJob entityId=TENANT001 taxRate=10.0 inputFile=data/input/employees.csv outputFile=disbursed_output.csv"
```

### Method 2: Using JAR File (Production)

#### Step 1: Build the JAR
```bash
mvn clean package
```

This creates: `target/fin-ap-spring-batch-1.0.0.jar`

#### Step 2: Execute the JAR

**Basic execution (shows help):**
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar
```

**List available jobs:**
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar list
```

**Run Payment Processing Job:**
```bash
# With entityId and default parameters (10% bonus)
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001

# With entityId and custom bonus percentage
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0

# With all parameters
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=20.0 inputFile=data/input/employees.csv outputFile=processed_output.csv
```

**Run Payment Disbursement Job:**
```bash
# With entityId and default parameters (5% tax rate)
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001

# With entityId and custom tax rate
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=7.5

# With all parameters
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=10.0 inputFile=data/input/employees.csv outputFile=disbursed_output.csv
```

## Available Jobs

1. **paymentProcessingJob**
   - Processes employee data and applies salary bonus
   - Output: `output/processed_employees.csv`

2. **paymentDisbursementJob**
   - Processes employee data and calculates net salary after tax
   - Output: `output/disbursed_employees.csv`

## Job Parameters

### Common Parameters (Required for Multitenancy)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `entityId` | String | **Yes** | Tenant identifier (entity_id) for multitenant operations |

### Payment Processing Job Parameters

| Parameter | Type | Default | Description |
|-----------|------|--------|-------------|
| `entityId` | String | **Required** | Tenant identifier |
| `bonusPercentage` | Double | 10.0 | Bonus percentage to apply to salary |
| `inputFile` | String | `data/input/employees.csv` | Input CSV file path |
| `outputFile` | String | `processed_employees.csv` | Output CSV file name |

### Payment Disbursement Job Parameters

| Parameter | Type | Default | Description |
|-----------|------|--------|-------------|
| `entityId` | String | **Required** | Tenant identifier |
| `taxRate` | Double | 5.0 | Tax rate percentage to deduct |
| `inputFile` | String | `data/input/employees.csv` | Input CSV file path |
| `outputFile` | String | `disbursed_employees.csv` | Output CSV file name |

## Examples

### Example 1: Run Payment Processing with 15% Bonus
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0
```

### Example 2: Run Payment Disbursement with 7.5% Tax Rate
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=7.5
```

### Example 3: Run with Custom Input and Output Files
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=20.0 inputFile=/path/to/input.csv outputFile=my_output.csv
```

### Example 4: Using Maven with Parameters
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="run paymentProcessingJob entityId=TENANT001 bonusPercentage=25.0 inputFile=data/input/employees.csv"
```

### Example 5: Multiple Tenants
```bash
# Run for Tenant 001
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0

# Run for Tenant 002
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT002 bonusPercentage=20.0
```

## Output Files

- **Payment Processing:** `output/processed_employees.csv`
- **Payment Disbursement:** `output/disbursed_employees.csv`

Output files are created in the `output/` directory in the project root.

## Logging

The application provides detailed logging at each stage:
- `[Job Name - STEP START]` - Step initialization
- `[Job Name - READ]` - Reading records
- `[Job Name - PROCESS]` - Processing records
- `[Job Name - WRITE]` - Writing records
- `[Job Name - STEP END]` - Step completion with statistics

## Troubleshooting

1. **Job not found error:**
   - Verify job name: `paymentProcessingJob` or `paymentDisbursementJob`
   - Use `list` command to see available jobs

2. **File not found:**
   - Ensure input CSV file exists at the specified path
   - Check file permissions

3. **Parameter parsing errors:**
   - Use format: `parameterName=value`
   - No spaces around `=`
   - For file paths with spaces, use quotes

4. **"No tenant context set" error:**
   - Ensure `entityId` parameter is provided when running jobs
   - Example: `entityId=TENANT001`
   - This parameter is required for all database operations

5. **Database connection errors:**
   - Verify PostgreSQL is running
   - Check database connection settings in `application.yml`
   - Ensure database `fin_ap_batch` exists
   - See `DATABASE_SETUP.md` for detailed setup instructions

## Help Command

To see usage information:
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar help
# or
java -jar target/fin-ap-spring-batch-1.0.0.jar
```
