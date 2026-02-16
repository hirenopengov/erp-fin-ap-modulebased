package com.opengov.erp.ap.job.paymentprocessing.reader;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class PaymentProcessingReader {

    private StepExecution stepExecution;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @StepScope
    public FlatFileItemReader<EmployeeCSVDTO> reader() {
        // Get input file from job parameters, default to classpath resource
        String inputFile = stepExecution != null && stepExecution.getJobParameters().getString("inputFile") != null
                ? stepExecution.getJobParameters().getString("inputFile")
                : Constants.FilePaths.INPUT_DIR + Constants.FilePaths.EMPLOYEES_CSV;

        Resource resource;
        if (inputFile.startsWith("classpath:") || (!inputFile.contains("/") && !inputFile.contains("\\"))) {
            // Use classpath resource
            String classpathFile = inputFile.startsWith("classpath:") ? inputFile.substring(10) : inputFile;
            resource = new ClassPathResource(classpathFile);
        } else {
            // Use file system resource
            resource = new FileSystemResource(inputFile);
        }

        return new FlatFileItemReaderBuilder<EmployeeCSVDTO>()
                .name("paymentProcessingReader")
                .resource(resource)
                .delimited()
                .names("id", "name", "department", "salary")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<EmployeeCSVDTO>() {{
                    setTargetType(EmployeeCSVDTO.class);
                }})
                .linesToSkip(1) // Skip header row
                .build();
    }
}
