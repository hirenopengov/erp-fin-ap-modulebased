# fin-ap-spring-batch

Spring Boot 3.3 application with Spring Batch containing multiple sample jobs.

## Requirements
- Java 22
- Maven 3.6+
- Spring Boot 3.3.0

## Project Structure

```
src/main/java/com/example/batch/
├── Application.java                    # Main Spring Boot application
├── common/                              # Common components
│   ├── config/
│   │   └── JpaConfig.java             # JPA configuration
│   ├── constants/
│   │   └── Constants.java             # Application constants
│   ├── dto/
│   │   ├── EmployeeDTO.java            # Employee DTO
│   │   └── EmployeeCSVDTO.java        # Employee CSV DTO
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   ├── mapper/
│   │   └── EmployeeMapper.java        # Entity-DTO mapper
│   ├── model/
│   │   ├── BaseEntity.java            # Base entity with common fields
│   │   └── Employee.java              # Employee entity
│   ├── repository/
│   │   ├── BaseRepository.java        # Base repository interface
│   │   └── EmployeeRepository.java    # Employee repository
│   ├── service/
│   │   ├── BaseService.java           # Base service class
│   │   └── EmployeeService.java       # Employee service
│   └── util/
│       ├── DateUtil.java              # Date utility
│       └── ValidationUtil.java        # Validation utility
└── job/
    ├── paymentprocessing/
    │   ├── config/
    │   │   └── PaymentProcessingConfig.java  # Payment processing job configuration
    │   ├── reader/
    │   │   └── PaymentProcessingReader.java  # Payment processing CSV reader
    │   ├── processor/
    │   │   └── PaymentProcessingProcessor.java  # Payment processing item processor
    │   └── writer/
    │       └── PaymentProcessingWriter.java   # Payment processing CSV writer
    └── paymentdisbursement/
        ├── config/
        │   └── PaymentDisbursementConfig.java  # Payment disbursement job configuration
        ├── reader/
        │   └── PaymentDisbursementReader.java  # Payment disbursement CSV reader
        ├── processor/
        │   └── PaymentDisbursementProcessor.java  # Payment disbursement item processor
        └── writer/
            └── PaymentDisbursementWriter.java   # Payment disbursement CSV writer

src/main/resources/
└── data/
    └── input/
        └── employees.csv              # Input CSV file for payment jobs
```

## Features

- **Payment Processing Job**: Reads employee data from CSV file (`employees.csv`), processes it (applies salary bonus and converts names to uppercase), and writes processed data to `output/processed_employees.csv`
  - Reader: Reads from `src/main/resources/data/input/employees.csv`
  - Processor: Applies 10% salary bonus and converts names to uppercase
  - Writer: Writes to `output/processed_employees.csv`
- **Payment Disbursement Job**: Reads employee data from CSV file (`employees.csv`), processes it (applies tax deduction to calculate net salary), and writes disbursed data to `output/disbursed_employees.csv`
  - Reader: Reads from `src/main/resources/data/input/employees.csv`
  - Processor: Applies 5% tax deduction to calculate net salary for disbursement
  - Writer: Writes to `output/disbursed_employees.csv`

## Running the Application

### Build the project
```bash
mvn clean install
```

### Run the application
```bash
mvn spring-boot:run
```

### Command-Based Job Execution

The application supports command-based job execution with parameters. After building the JAR, you can run jobs with parameters:

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
# Run with default parameters (10% bonus)
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob

# Run with custom bonus percentage
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob bonusPercentage=15.0

# Run with custom input and output files
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentProcessingJob bonusPercentage=20.0 inputFile=data/input/employees.csv outputFile=custom_output.csv
```

**Payment Disbursement Job:**
```bash
# Run with default parameters (5% tax rate)
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob

# Run with custom tax rate
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob taxRate=7.5

# Run with custom input and output files
java -jar target/fin-ap-spring-batch-1.0.0.jar run paymentDisbursementJob taxRate=10.0 inputFile=data/input/employees.csv outputFile=disbursed_output.csv
```

#### Available Parameters

**Payment Processing Job:**
- `bonusPercentage` (Double) - Bonus percentage to apply (default: 10.0)
- `inputFile` (String) - Input CSV file path (default: data/input/employees.csv)
- `outputFile` (String) - Output CSV file name (default: processed_employees.csv)

**Payment Disbursement Job:**
- `taxRate` (Double) - Tax rate percentage (default: 5.0)
- `inputFile` (String) - Input CSV file path (default: data/input/employees.csv)
- `outputFile` (String) - Output CSV file name (default: disbursed_employees.csv)

## Architecture

The project follows a layered architecture:

- **Model Layer** (`common/model`): JPA entities extending `BaseEntity`
- **Repository Layer** (`common/repository`): JPA repositories for data access
- **Service Layer** (`common/service`): Business logic services extending `BaseService`
- **DTO Layer** (`common/dto`): Data Transfer Objects for API/CSV processing
- **Mapper Layer** (`common/mapper`): Entity-DTO mappers
- **Exception Handling** (`common/exception`): Custom exceptions and global handler
- **Utilities** (`common/util`): Common utility classes
- **Constants** (`common/constants`): Application-wide constants

## Configuration

The application uses H2 in-memory database for Spring Batch metadata and JPA entities. You can access the H2 console at:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:batchdb
- Username: sa
- Password: (empty)

## Notes

- Jobs are disabled by default on startup (set `spring.batch.job.enabled=false` in application.yml)
- To run jobs, either enable them in application.yml or pass job names as command-line arguments
- Payment Processing Job requires the input CSV file at `src/main/resources/data/input/employees.csv`
- Payment Processing Job output will be written to `output/processed_employees.csv` in the project root directory
- Payment Disbursement Job requires the input CSV file at `src/main/resources/data/input/employees.csv`
- Payment Disbursement Job output will be written to `output/disbursed_employees.csv` in the project root directory
- All job components (reader, processor, writer, config) are organized in their respective job folders
