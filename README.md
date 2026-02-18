# ERP Financial AP - Spring Batch Module

Enterprise Resource Planning (ERP) Financial Accounts Payable batch processing module built with Spring Boot 3.3 and Spring Batch 5.x. This module provides batch processing capabilities for payment processing, disbursement operations, and payable summary generation with multitenancy support and external vendor API integration.

## Requirements

- **Java 22**
- **Maven 3.6+**
- **Spring Boot 3.3.0**
- **Spring Batch 5.1.2**
- **PostgreSQL 12+** (for batch metadata and application data)
- **Hibernate 6.x** (via Spring Data JPA)

## Project Structure

```
src/main/java/com/opengov/erp/ap/
├── Application.java                          # Main Spring Boot application
├── common/                                    # Common components
│   ├── config/
│   │   ├── BatchConfig.java                  # Spring Batch configuration
│   │   ├── JpaConfig.java                    # JPA configuration
│   │   └── TenantAspect.java                 # Multitenancy aspect
│   ├── constants/
│   │   └── Constants.java                    # Application constants
│   ├── context/
│   │   └── TenantContext.java                # Tenant context holder
│   ├── dto/
│   │   ├── EmployeeDTO.java                  # Employee DTO
│   │   ├── EmployeeCSVDTO.java               # Employee CSV DTO
│   │   ├── PayableSummaryCSVDTO.java         # Payable Summary CSV DTO
│   │   ├── VendorDTO.java                    # Vendor DTO
│   │   ├── VendorAddressDTO.java            # Vendor Address DTO
│   │   └── VendorContactDTO.java            # Vendor Contact DTO
│   ├── exception/
│   │   ├── ResourceNotFoundException.java   # Custom exception
│   │   └── GlobalExceptionHandler.java       # Global exception handler
│   ├── mapper/
│   │   └── EmployeeMapper.java               # Entity-DTO mapper
│   ├── model/
│   │   ├── BaseEntity.java                   # Base entity with common fields
│   │   └── Employee.java                     # Employee entity
│   ├── repository/
│   │   ├── BaseRepository.java               # Base repository interface
│   │   └── EmployeeRepository.java           # Employee repository
│   ├── runner/
│   │   └── JobCommandLineRunner.java         # Command-line job runner
│   ├── service/
│   │   ├── BaseService.java                  # Base service class
│   │   ├── EmployeeService.java              # Employee service
│   │   ├── VendorService.java                # Vendor service (external API integration)
│   │   └── JobLauncherService.java           # Job launcher service
│   └── util/
│       ├── DateUtil.java                     # Date utility
│       └── ValidationUtil.java               # Validation utility
└── job/
    ├── paymentprocessing/
    │   ├── config/
    │   │   └── PaymentProcessingConfig.java  # Payment processing job configuration
    │   ├── listener/
    │   │   ├── PaymentProcessingItemReadListener.java    # Item read listener
    │   │   ├── PaymentProcessingItemProcessListener.java # Item process listener
    │   │   ├── PaymentProcessingItemWriteListener.java  # Item write listener
    │   │   └── PaymentProcessingStepListener.java        # Step execution listener
    │   ├── processor/
    │   │   └── PaymentProcessingProcessor.java           # Payment processing item processor
    │   ├── reader/
    │   │   └── PaymentProcessingReader.java              # Payment processing CSV reader
    │   └── writer/
    │       └── PaymentProcessingWriter.java             # Payment processing CSV writer
    ├── paymentdisbursement/
    │   ├── config/
    │   │   └── PaymentDisbursementConfig.java            # Payment disbursement job configuration
    │   ├── listener/
    │   │   ├── PaymentDisbursementItemReadListener.java  # Item read listener
    │   │   ├── PaymentDisbursementItemProcessListener.java # Item process listener
    │   │   ├── PaymentDisbursementItemWriteListener.java   # Item write listener
    │   │   └── PaymentDisbursementStepListener.java         # Step execution listener
    │   ├── processor/
    │   │   └── PaymentDisbursementProcessor.java          # Payment disbursement item processor
    │   ├── reader/
    │   │   └── PaymentDisbursementReader.java             # Payment disbursement CSV reader
    │   └── writer/
    │       └── PaymentDisbursementWriter.java            # Payment disbursement CSV writer
    └── payablesummary/
        ├── config/
        │   └── PayableSummaryConfig.java                 # Payable summary job configuration
        ├── listener/
        │   ├── PayableSummaryItemReadListener.java       # Item read listener
        │   ├── PayableSummaryItemProcessListener.java    # Item process listener
        │   ├── PayableSummaryItemWriteListener.java      # Item write listener
        │   └── PayableSummaryStepListener.java           # Step execution listener
        ├── processor/
        │   └── PayableSummaryProcessor.java              # Payable summary item processor
        ├── reader/
        │   └── PayableSummaryReader.java                 # Payable summary database reader
        └── writer/
            └── PayableSummaryWriter.java                 # Payable summary CSV writer

src/main/resources/
├── application.yml                           # Application configuration
└── data/
    └── input/
        └── employees.csv                     # Input CSV file for payment jobs
```

## Features

### Payment Processing Job
Processes employee payment data by applying bonuses and formatting employee information.

- **Reader**: Reads employee data from CSV file using `FlatFileItemReader` (supports classpath, absolute paths, and `classpath:` prefix)
- **Processor**: 
  - Applies configurable bonus percentage to employee salaries (default: 10%)
  - Converts employee names to uppercase
  - Supports flexible parameter type handling (Double, Long, or String)
- **Writer**: Writes processed data to `output/processed_employees.csv` using `FlatFileItemWriter`
- **Listeners**: 
  - Item read listener for tracking read operations
  - Item process listener for tracking processing operations
  - Item write listener for tracking write operations
  - Step execution listener for tracking step metrics and duration

### Payment Disbursement Job
Processes employee payment disbursement by calculating net salary after tax deductions.

- **Reader**: Reads employee data from CSV file (`data/input/employees.csv`)
- **Processor**: 
  - Applies configurable tax rate to calculate net salary
  - Calculates disbursement amounts
- **Writer**: Writes disbursed data to `output/disbursed_employees.csv`
- **Listeners**: 
  - Item read listener for tracking read operations
  - Item process listener for tracking processing operations
  - Item write listener for tracking write operations
  - Step execution listener for tracking step metrics and duration

### Payable Summary Job
Generates a summary of payables aggregated by vendor from the database, enriched with vendor information from external API.

- **Reader**: Reads payable data from PostgreSQL database using `JdbcCursorItemReader`
  - Aggregates payables by vendor from `pr_ap_payable_run_item`, `pr_ap_payable`, and `pr_ap_invoice_header` tables
  - Filters by `paymentRunId` and `entityId` parameters
  - Calculates total payable amount and invoice count per vendor
- **Processor**: 
  - Generates unique reference number for each vendor row
  - Sets status to "DRAFT"
  - Adds timestamp (UTC) when file is generated
  - Fetches vendor address from external Vendor API using `VendorService`
  - Validates vendor existence before fetching address
- **Writer**: Writes summary data to `output/PayableSummary.csv`
  - Custom field extractor formats `dateCreated` as ISO-8601 string
  - Output columns: `vendorId`, `vendorNumber`, `totalPayableAmount`, `invoiceCount`, `referenceNumber`, `status`, `dateCreated`, `address`
- **Listeners**: 
  - Item read listener for tracking read operations
  - Item process listener for tracking processing operations
  - Item write listener for tracking write operations
  - Step execution listener for tracking step metrics and duration

### External Vendor API Integration
The module integrates with an external Vendor API for fetching vendor information:

- **VendorService**: Service for vendor-related operations
  - Checks vendor existence using HEAD requests
  - Fetches vendor details including addresses
  - API Endpoint: `GET /api/v1/entities/:entityId/vendors/:id`
  - Supports optional `include` parameter for field selection
- **Configuration**: Vendor API settings in `application.yml`
  - `vendor.api.base-url`: Base URL for vendor API (default: `http://test.vendor.api`)
  - `vendor.api.timeout`: Request timeout in milliseconds (default: 5000)

### Multitenancy Support
- **Tenant Context**: Thread-local tenant context management
- **Tenant Aspect**: AOP-based tenant filtering for database queries
- **Entity ID Parameter**: Jobs accept `entityId` parameter for tenant isolation

## Building the Project

### Build the JAR file
```bash
mvn clean package -DskipTests
```

This will create an executable JAR file at `target/fin-ap-spring-batch-1.0.0.jar`.

## Running the Application

### Command-Line Job Execution

The application supports command-line job execution with parameters. After building the JAR, you can run jobs with parameters:

#### Basic Usage
```bash
java -jar target/fin-ap-spring-batch-1.0.0.jar run <jobName> [param1=value1] [param2=value2] ...
```

#### Available Commands
- `run <jobName> [params]` - Run a specific job with optional parameters
- `list` - List all available jobs
- `help` - Show help message

#### Examples

**Payment Processing Job:**
```bash
# Run with default parameters (10% bonus) and tenant context
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001

# Run with custom bonus percentage
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=15.0

# Run with custom input and output files
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob entityId=TENANT001 bonusPercentage=20.0 inputFile=data/input/employees.csv outputFile=custom_output.csv
```

**Payment Disbursement Job:**
```bash
# Run with default parameters (5% tax rate) and tenant context
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001

# Run with custom tax rate
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=7.5

# Run with custom input and output files
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob entityId=TENANT001 taxRate=10.0 inputFile=data/input/employees.csv outputFile=disbursed_output.csv
```

**Payable Summary Job:**
```bash
# Run with required parameters (paymentRunId and entityId)
java -jar target/fin-ap-spring-batch-1.0.0.jar run payableSummaryJob entityId=550e8400-e29b-41d4-a716-446655440000 paymentRunId=123e4567-e89b-12d3-a456-426614174000

# Example with UUID parameters
java -jar target/fin-ap-spring-batch-1.0.0.jar run payableSummaryJob entityId=550e8400-e29b-41d4-a716-446655440000 paymentRunId=123e4567-e89b-12d3-a456-426614174000
```

#### Available Parameters

**Common Parameters (all jobs):**
- `entityId` (String) - **Required** - Tenant/entity identifier for multitenancy support

**Payment Processing Job:**
- `bonusPercentage` (Double) - Bonus percentage to apply (default: 10.0)
- `inputFile` (String) - Input CSV file path (default: `data/input/employees.csv`)
  - Supports classpath resources (e.g., `data/input/employees.csv`)
  - Supports absolute file paths (e.g., `C:/data/employees.csv`)
  - Supports `classpath:` prefix (e.g., `classpath:data/input/employees.csv`)
- `outputFile` (String) - Output CSV file name (default: `processed_employees.csv`)
  - Output files are written to the `output/` directory

**Payment Disbursement Job:**
- `taxRate` (Double) - Tax rate percentage (default: 5.0)
- `inputFile` (String) - Input CSV file path (default: `data/input/employees.csv`)
  - Supports classpath resources (e.g., `data/input/employees.csv`)
  - Supports absolute file paths (e.g., `C:/data/employees.csv`)
  - Supports `classpath:` prefix (e.g., `classpath:data/input/employees.csv`)
- `outputFile` (String) - Output CSV file name (default: `disbursed_employees.csv`)
  - Output files are written to the `output/` directory

**Payable Summary Job:**
- `paymentRunId` (UUID String) - **Required** - Payment run identifier for filtering payables
- `entityId` (UUID String) - **Required** - Tenant/entity identifier for multitenancy support and vendor API calls
- Output file is always written to `output/PayableSummary.csv` (not configurable)

## Architecture

The project follows a layered architecture with multitenancy support:

- **Model Layer** (`common/model`): JPA entities extending `BaseEntity` with tenant isolation
- **Repository Layer** (`common/repository`): JPA repositories for data access with tenant filtering
- **Service Layer** (`common/service`): Business logic services extending `BaseService`
- **DTO Layer** (`common/dto`): Data Transfer Objects for API/CSV processing
- **Mapper Layer** (`common/mapper`): Entity-DTO mappers
- **Exception Handling** (`common/exception`): Custom exceptions and global handler
- **Utilities** (`common/util`): Common utility classes
- **Constants** (`common/constants`): Application-wide constants
- **Context** (`common/context`): Tenant context management
- **Aspect** (`common/config`): AOP-based tenant filtering
- **Runner** (`common/runner`): Command-line job execution

### Batch Job Architecture

Each batch job follows the Spring Batch chunk-oriented processing pattern:

1. **Reader**: Step-scoped bean that reads data from various sources
   - **CSV Files**: `FlatFileItemReader` for CSV-based jobs (Payment Processing, Payment Disbursement)
   - **Database**: `JdbcCursorItemReader` for database-based jobs (Payable Summary)
2. **Processor**: Processes items with business logic (bonus calculation, tax deduction, vendor enrichment)
   - Can integrate with external APIs (e.g., VendorService for vendor address lookup)
3. **Writer**: Step-scoped bean that writes processed data to CSV files
   - Uses `FlatFileItemWriter` with custom field extractors for complex data formatting
4. **Listeners**: Track job execution metrics and provide logging
   - Item-level listeners (read, process, write)
   - Step-level listeners for overall step metrics
5. **Config**: Defines job and step configuration with step-scoped beans
   - Uses `@StepScope` for readers, writers, and processors that need job parameters

## Configuration

### Database Configuration

The application uses **PostgreSQL** for both Spring Batch metadata and application data:

- **Database**: PostgreSQL (default port: 5433)
- **Database Name**: `erp_fin_ap_phase1`
- **Username**: `postgres`
- **Password**: `postgres`

Update `src/main/resources/application.yml` to configure your database connection:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/erp_fin_ap_phase1
    username: postgres
    password: postgres
```

### Vendor API Configuration

The application integrates with an external Vendor API. Configure the API settings in `application.yml`:

```yaml
vendor:
  api:
    base-url: http://test.vendor.api
    timeout: 5000
```

- **base-url**: Base URL for the vendor API endpoint
- **timeout**: Request timeout in milliseconds

### Spring Batch Configuration

- **Job Execution**: Disabled by default (`spring.batch.job.enabled=false`)
- **Schema Initialization**: Always initialize batch metadata schema (`spring.batch.jdbc.initialize-schema: always`)
- **Chunk Size**: Default chunk size is 10 items per transaction

### Hibernate Configuration

- **DDL Mode**: `update` - Automatically updates database schema
- **Dialect**: PostgreSQL dialect (explicitly configured for Hibernate 6.x)
- **Batch Size**: 20 items per batch insert/update
- **SQL Logging**: Enabled for debugging

## Input File Format

The input CSV file (`data/input/employees.csv`) should have the following format:

```csv
id,name,department,salary
1,John Doe,Engineering,50000.00
2,Jane Smith,Marketing,45000.00
3,Bob Johnson,Sales,40000.00
```

**Required Columns:**
- `id` - Employee ID (Integer)
- `name` - Employee name (String)
- `department` - Department name (String)
- `salary` - Employee salary (Double)

## Output Files

Output files are written to the `output/` directory in the project root:

- **Payment Processing**: `output/processed_employees.csv`
  - Columns: `id`, `name`, `department`, `salary`
- **Payment Disbursement**: `output/disbursed_employees.csv`
  - Columns: `id`, `name`, `department`, `salary`
- **Payable Summary**: `output/PayableSummary.csv`
  - Columns: `vendorId`, `vendorNumber`, `totalPayableAmount`, `invoiceCount`, `referenceNumber`, `status`, `dateCreated`, `address`
  - `dateCreated` is formatted as ISO-8601 timestamp string
  - `address` is fetched from external Vendor API

Output files include headers and maintain consistent column structures.

## Technical Notes

### Spring Batch 5.x Compatibility

- **ItemWriteListener**: `beforeWrite`, `afterWrite`, and `onWriteError` are default methods (no `@Override` needed)
- **Step Execution Times**: Uses `LocalDateTime` instead of `Date` for step execution times
- **Page API**: Uses `PageImpl` instead of `Page.of()` for pagination

### Hibernate 6.x Compatibility

- **Column Definitions**: Uses `columnDefinition` for numeric precision/scale instead of `precision` and `scale` attributes
- **Dialect**: Explicitly configured PostgreSQL dialect (auto-detection may fail without JDBC metadata)

### Resource Handling

- **Classpath Resources**: When running from JAR, relative paths (e.g., `data/input/employees.csv`) are treated as classpath resources
- **File System Resources**: Absolute paths and paths with `/` or `\` are treated as file system resources
- **Step-Scoped Beans**: Readers and writers are step-scoped to properly handle job parameters

### Web Server

- **Automatic Disabling**: Web server is automatically disabled when command-line arguments are provided
- **No Port Conflicts**: Prevents port conflicts when running batch jobs from command line

## Troubleshooting

### Common Issues

1. **Bean Definition Conflicts**: Ensure `@Component` annotations are removed from reader and writer classes (they should be step-scoped beans)

2. **Resource Not Found**: Verify the input CSV file exists in `src/main/resources/data/input/employees.csv` or provide an absolute path

3. **Database Connection**: Ensure PostgreSQL is running and accessible at the configured URL

4. **Parameter Type Errors**: Job parameters can be passed as strings, numbers, or dates - the application handles type conversion automatically

5. **Multitenancy**: Always provide `entityId` parameter when running jobs to ensure proper tenant isolation

6. **Vendor API Connection**: For Payable Summary job, ensure the vendor API is accessible and configured correctly in `application.yml`. If the API is unavailable, the job will continue but vendor addresses will be empty.

7. **Database Tables**: Payable Summary job requires specific database tables (`pr_ap_payable_run_item`, `pr_ap_payable`, `pr_ap_invoice_header`). Ensure these tables exist and contain data before running the job.

## Dependencies

Key dependencies include:

- Spring Boot 3.3.0
- Spring Batch 5.1.2
- Spring Data JPA
- PostgreSQL Driver
- Hibernate 6.x
- Spring AOP (for multitenancy aspects)
- Spring Web (for exception handling and REST client)
- Spring WebFlux / RestClient (for external API integration)

See `pom.xml` for complete dependency list.

## License

[Add your license information here]

## Contributing

[Add contribution guidelines here]
