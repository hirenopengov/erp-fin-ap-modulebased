# Batch Job Documentation

## Overview

This ERP Financial AP module contains two Spring Batch jobs designed for processing employee payments through a two-stage workflow:

1. **Payment Processing Job** - Calculates gross pay with bonuses
2. **Payment Disbursement Job** - Calculates net pay after tax deductions

Both jobs are designed to work independently or sequentially as part of a complete payment processing workflow.

---

## Table of Contents

1. [Payment Processing Job](#payment-processing-job)
2. [Payment Disbursement Job](#payment-disbursement-job)
3. [Workflow Relationship](#workflow-relationship)
4. [Job Execution](#job-execution)
5. [Technical Architecture](#technical-architecture)
6. [Examples](#examples)

---

## Payment Processing Job

### Purpose

The Payment Processing Job processes employee payment data by applying bonuses and formatting employee information. This job calculates the **gross pay** (salary + bonuses) for employees.

### Job Name
```
paymentProcessingJob
```

### Functionality

#### Input
- Reads employee data from CSV file (`data/input/employees.csv` by default)
- CSV format: `id,name,department,salary`

#### Processing Logic

1. **Bonus Calculation**
   - Applies a configurable bonus percentage to employee salaries
   - Default bonus: **10%**
   - Formula: `New Salary = Original Salary + (Original Salary × bonusPercentage / 100)`
   - Example: $50,000 salary with 10% bonus = $55,000

2. **Name Formatting**
   - Converts all employee names to **UPPERCASE**
   - Example: "John Doe" → "JOHN DOE"

#### Output
- Writes processed data to `output/processed_employees.csv`
- Output format: `id,name,department,salary` (with updated values)

### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `entityId` | String | **Yes** | - | Tenant/entity identifier for multitenancy |
| `bonusPercentage` | Double | No | 10.0 | Bonus percentage to apply (e.g., 15.0 for 15%) |
| `inputFile` | String | No | `data/input/employees.csv` | Input CSV file path (classpath or absolute) |
| `outputFile` | String | No | `processed_employees.csv` | Output CSV file name (written to `output/` directory) |

### Example Execution

```bash
# Basic execution with default 10% bonus
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001

# With custom bonus percentage (15%)
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0

# With custom input and output files
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=20.0 inputFile=data/input/employees.csv outputFile=custom_processed.csv
```

### Input/Output Example

**Input (`employees.csv`):**
```csv
id,name,department,salary
1,John Doe,Engineering,50000.00
2,Jane Smith,Marketing,45000.00
3,Bob Johnson,Sales,40000.00
```

**Output (`processed_employees.csv`) - with 10% bonus:**
```csv
id,name,department,salary
1,JOHN DOE,Engineering,55000.00
2,JANE SMITH,Marketing,49500.00
3,BOB JOHNSON,Sales,44000.00
```

---

## Payment Disbursement Job

### Purpose

The Payment Disbursement Job calculates the **net pay** (salary after tax deductions) for employee payment disbursement. This job determines the actual amount to be paid to employees after tax withholdings.

### Job Name
```
paymentDisbursementJob
```

### Functionality

#### Input
- Reads employee data from CSV file (`data/input/employees.csv` by default)
- CSV format: `id,name,department,salary`

#### Processing Logic

1. **Tax Deduction Calculation**
   - Applies a configurable tax rate to employee salaries
   - Default tax rate: **5%**
   - Formula: `Net Salary = Original Salary - (Original Salary × taxRate / 100)`
   - Example: $50,000 salary with 5% tax = $47,500 net pay

2. **Net Pay Calculation**
   - Calculates the final disbursement amount
   - This is the amount that will actually be paid to the employee

#### Output
- Writes disbursed data to `output/disbursed_employees.csv`
- Output format: `id,name,department,salary` (salary represents net pay)

### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `entityId` | String | **Yes** | - | Tenant/entity identifier for multitenancy |
| `taxRate` | Double | No | 5.0 | Tax rate percentage to deduct (e.g., 7.5 for 7.5%) |
| `inputFile` | String | No | `data/input/employees.csv` | Input CSV file path (classpath or absolute) |
| `outputFile` | String | No | `disbursed_employees.csv` | Output CSV file name (written to `output/` directory) |

### Example Execution

```bash
# Basic execution with default 5% tax rate
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001

# With custom tax rate (7.5%)
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=7.5

# With custom input and output files
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=10.0 inputFile=data/input/employees.csv outputFile=custom_disbursed.csv
```

### Input/Output Example

**Input (`employees.csv`):**
```csv
id,name,department,salary
1,John Doe,Engineering,50000.00
2,Jane Smith,Marketing,45000.00
3,Bob Johnson,Sales,40000.00
```

**Output (`disbursed_employees.csv`) - with 5% tax:**
```csv
id,name,department,salary
1,John Doe,Engineering,47500.00
2,Jane Smith,Marketing,42750.00
3,Bob Johnson,Sales,38000.00
```

---

## Workflow Relationship

### Sequential Workflow

These jobs are designed to work together in a complete payment processing workflow:

```
┌─────────────────────────────────────────────────────────────┐
│  Step 1: Payment Processing Job                          │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ Input:  employees.csv                               │  │
│  │ Process: Apply bonuses (e.g., 10%)                 │  │
│  │         Convert names to UPPERCASE                   │  │
│  │ Output: processed_employees.csv (GROSS PAY)        │  │
│  └─────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────────┐
│  Step 2: Payment Disbursement Job                         │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ Input:  processed_employees.csv (or employees.csv) │  │
│  │ Process: Apply tax deductions (e.g., 5%)           │  │
│  │ Output: disbursed_employees.csv (NET PAY)        │  │
│  └─────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Complete Example Workflow

**Scenario**: Process payments for employees with 10% bonus and 5% tax deduction

1. **Run Payment Processing Job:**
   ```bash
   java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=10.0
   ```
   - Result: `output/processed_employees.csv` with gross pay

2. **Run Payment Disbursement Job:**
   ```bash
   # Option A: Use processed file as input
   java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=5.0 inputFile=output/processed_employees.csv
   
   # Option B: Use original file (if you want to apply tax to original salary)
   java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=5.0
   ```
   - Result: `output/disbursed_employees.csv` with net pay

### Use Cases

#### Use Case 1: Independent Execution
- **Payment Processing Job**: Use when you only need to calculate gross pay with bonuses
- **Payment Disbursement Job**: Use when you only need to calculate net pay after taxes

#### Use Case 2: Sequential Execution
- Run both jobs in sequence for complete payment processing:
  1. Calculate gross pay (with bonuses)
  2. Calculate net pay (after taxes) from gross pay

#### Use Case 3: Different Tax Rates per Tenant
- Use `entityId` parameter to process different tenants with different tax rates:
  ```bash
  # Tenant 001: 5% tax
  java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=5.0
  
  # Tenant 002: 7.5% tax
  java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT002 taxRate=7.5
  ```

---

## Job Execution

### Prerequisites

1. **Database**: PostgreSQL must be running and accessible
2. **Input File**: CSV file must exist at the specified path
3. **Output Directory**: `output/` directory will be created automatically if it doesn't exist

### Execution Methods

#### Method 1: Command Line (Recommended)

```bash
# Build the JAR
mvn clean package -DskipTests

# Run job
java -jar target/fin-ap-spring-batch-1.0.0.jar run <jobName> [parameters]
```

#### Method 2: Maven Spring Boot Plugin

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="run <jobName> [parameters]"
```

### Available Commands

- `run <jobName> [params]` - Execute a specific job with optional parameters
- `list` - List all available jobs
- `help` - Show help message

### Parameter Format

Parameters are passed as `key=value` pairs:
- String values: `entityId=TENANT001`
- Numeric values: `bonusPercentage=15.0` or `taxRate=7.5`
- File paths: `inputFile=data/input/employees.csv`

### Resource Path Handling

The jobs support multiple resource path formats:

1. **Classpath Resources** (default):
   - `data/input/employees.csv` - Relative classpath
   - `classpath:data/input/employees.csv` - Explicit classpath prefix

2. **File System Resources**:
   - `C:/data/employees.csv` - Absolute Windows path
   - `/data/employees.csv` - Absolute Unix path

**Note**: When running from a JAR file, relative paths are automatically treated as classpath resources.

---

## Technical Architecture

### Job Components

Each job consists of the following Spring Batch components:

#### 1. Reader (`FlatFileItemReader`)
- **Type**: Step-scoped bean
- **Function**: Reads CSV data row by row
- **Configuration**: 
  - Delimited format (comma-separated)
  - Skips header row
  - Maps to `EmployeeCSVDTO` objects

#### 2. Processor (`ItemProcessor`)
- **Type**: Singleton component
- **Function**: Applies business logic (bonus calculation or tax deduction)
- **Payment Processing**: Applies bonus percentage and converts names to uppercase
- **Payment Disbursement**: Applies tax rate to calculate net salary

#### 3. Writer (`FlatFileItemWriter`)
- **Type**: Step-scoped bean
- **Function**: Writes processed data to CSV file
- **Configuration**:
  - Delimited format (comma-separated)
  - Includes header row
  - Writes to `output/` directory

#### 4. Listeners

Each job includes four types of listeners:

1. **ItemReadListener**: Tracks items read from input
2. **ItemProcessListener**: Tracks items processed
3. **ItemWriteListener**: Tracks items written to output
4. **StepExecutionListener**: Tracks step metrics (duration, read count, write count, etc.)

### Chunk Processing

- **Chunk Size**: 10 items per transaction (configurable via `Constants.BatchJob.DEFAULT_CHUNK_SIZE`)
- **Transaction Management**: Each chunk is processed in a single transaction
- **Error Handling**: Failed chunks can be retried or skipped based on configuration

### Multitenancy Support

- **Tenant Context**: Managed via `TenantContext` thread-local variable
- **Tenant Aspect**: AOP-based filtering ensures data isolation
- **Entity ID**: Required parameter (`entityId`) for tenant identification

### Step Scope

Readers and writers are **step-scoped** beans, which means:
- They are created once per job execution
- They can access job parameters via `@Value("#{jobParameters['paramName']}")`
- They are destroyed after step completion

---

## Examples

### Example 1: Basic Payment Processing

**Goal**: Apply 15% bonus to all employees

```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0
```

**Result**: 
- Input: `employees.csv` with original salaries
- Output: `processed_employees.csv` with salaries increased by 15% and names in uppercase

### Example 2: Basic Payment Disbursement

**Goal**: Calculate net pay with 7.5% tax

```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=7.5
```

**Result**:
- Input: `employees.csv` with original salaries
- Output: `disbursed_employees.csv` with net salaries (after 7.5% tax deduction)

### Example 3: Complete Payment Workflow

**Goal**: Process payments with 10% bonus, then calculate net pay with 5% tax

**Step 1 - Process Payments:**
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=10.0
```

**Step 2 - Calculate Disbursement:**
```bash
# Use the processed file as input for disbursement
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=5.0 inputFile=output/processed_employees.csv outputFile=final_disbursement.csv
```

**Result**:
- `processed_employees.csv`: Gross pay (salary + 10% bonus)
- `final_disbursement.csv`: Net pay (gross pay - 5% tax)

### Example 4: Custom File Paths

**Goal**: Process a custom input file and write to a custom output location

```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=12.5 inputFile=C:/data/custom_employees.csv outputFile=monthly_processed.csv
```

**Result**:
- Reads from: `C:/data/custom_employees.csv`
- Writes to: `output/monthly_processed.csv`

### Example 5: Multiple Tenants

**Goal**: Process payments for different tenants with different rates

```bash
# Tenant 001: 10% bonus, 5% tax
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=10.0
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=5.0

# Tenant 002: 15% bonus, 7.5% tax
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT002 bonusPercentage=15.0
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT002 taxRate=7.5
```

---

## Calculation Formulas

### Payment Processing Job

```
Gross Salary = Original Salary + (Original Salary × bonusPercentage / 100)

Example:
  Original Salary: $50,000.00
  Bonus Percentage: 10%
  Gross Salary = $50,000.00 + ($50,000.00 × 10 / 100)
              = $50,000.00 + $5,000.00
              = $55,000.00
```

### Payment Disbursement Job

```
Net Salary = Original Salary - (Original Salary × taxRate / 100)

Example:
  Original Salary: $50,000.00
  Tax Rate: 5%
  Net Salary = $50,000.00 - ($50,000.00 × 5 / 100)
             = $50,000.00 - $2,500.00
             = $47,500.00
```

### Combined Workflow

```
Step 1: Gross Salary = Original Salary + (Original Salary × bonusPercentage / 100)
Step 2: Net Salary = Gross Salary - (Gross Salary × taxRate / 100)

Example:
  Original Salary: $50,000.00
  Bonus Percentage: 10%
  Tax Rate: 5%
  
  Gross Salary = $50,000.00 + ($50,000.00 × 10 / 100) = $55,000.00
  Net Salary = $55,000.00 - ($55,000.00 × 5 / 100) = $52,250.00
```

---

## Troubleshooting

### Common Issues

#### Issue 1: Input File Not Found
**Error**: `Input resource must exist (reader is in 'strict' mode)`

**Solution**:
- Verify the input file exists at the specified path
- Check if running from JAR - use classpath paths like `data/input/employees.csv`
- For absolute paths, ensure the file exists at that location

#### Issue 2: Parameter Type Error
**Error**: `Key bonusPercentage is not of type String`

**Solution**:
- The application handles type conversion automatically
- Parameters can be passed as strings: `bonusPercentage=15.0`
- The job will convert them to the appropriate type

#### Issue 3: Missing entityId Parameter
**Error**: Job fails or tenant context not set

**Solution**:
- Always provide `entityId` parameter: `entityId=TENANT001`
- This is required for multitenancy support

#### Issue 4: Output Directory Not Created
**Error**: Cannot write to output file

**Solution**:
- The `output/` directory is created automatically
- Ensure write permissions in the project directory
- Check disk space availability

### Debugging Tips

1. **Enable Debug Logging**: Set logging level to DEBUG in `application.yml`
2. **Check Step Metrics**: Review listener logs for read/write counts
3. **Verify CSV Format**: Ensure input CSV matches expected format (id,name,department,salary)
4. **Check Database**: Verify PostgreSQL connection and batch metadata tables

---

## Best Practices

1. **Always Provide entityId**: Required for multitenancy and data isolation
2. **Use Meaningful Output Names**: Use descriptive output file names for different runs
3. **Validate Input Data**: Ensure CSV files are properly formatted before processing
4. **Monitor Job Execution**: Use listeners to track job progress and metrics
5. **Test with Small Datasets**: Test jobs with small CSV files before processing large datasets
6. **Backup Input Files**: Keep backups of original input files
7. **Review Output Files**: Verify output files contain expected results

---

## Related Documentation

- [README.md](README.md) - Project overview and setup
- [JOB_EXECUTION_GUIDE.md](JOB_EXECUTION_GUIDE.md) - Detailed job execution guide
- [DATABASE_SETUP.md](DATABASE_SETUP.md) - Database setup instructions

---

## Support

For issues or questions, please refer to:
- Project README for setup instructions
- Application logs for detailed error messages
- Spring Batch documentation for framework-specific questions


