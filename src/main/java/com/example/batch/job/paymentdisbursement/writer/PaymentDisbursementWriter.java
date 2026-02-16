package com.opengov.erp.ap.job.paymentdisbursement.writer;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class PaymentDisbursementWriter {

    private StepExecution stepExecution;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public FlatFileItemWriter<EmployeeCSVDTO> writer() {
        // Get output file from job parameters, default to disbursed_employees.csv
        String outputFile = stepExecution != null && stepExecution.getJobParameters().getString("outputFile") != null
                ? stepExecution.getJobParameters().getString("outputFile")
                : Constants.FilePaths.DISBURSED_EMPLOYEES_CSV;

        String outputPath = Paths.get(Constants.FilePaths.OUTPUT_DIR, outputFile).toString();
        
        return new FlatFileItemWriterBuilder<EmployeeCSVDTO>()
                .name("paymentDisbursementWriter")
                .resource(new FileSystemResource(outputPath))
                .delimited()
                .delimiter(",")
                .names("id", "name", "department", "salary")
                .headerCallback(writer -> writer.write("id,name,department,salary"))
                .build();
    }
}
