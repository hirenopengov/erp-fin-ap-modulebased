package com.opengov.erp.ap.job.paymentdisbursement.config;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import com.opengov.erp.ap.common.validator.EntityIdJobParametersValidator;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementItemProcessListener;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementItemReadListener;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementItemWriteListener;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementStepListener;
import com.opengov.erp.ap.job.paymentdisbursement.processor.PaymentDisbursementProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PaymentDisbursementConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PaymentDisbursementProcessor paymentDisbursementProcessor;
    private final PaymentDisbursementStepListener paymentDisbursementStepListener;
    private final PaymentDisbursementItemReadListener paymentDisbursementItemReadListener;
    private final PaymentDisbursementItemProcessListener paymentDisbursementItemProcessListener;
    private final PaymentDisbursementItemWriteListener paymentDisbursementItemWriteListener;
    
    @Autowired
    private ApplicationContext applicationContext;

    public PaymentDisbursementConfig(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     PaymentDisbursementProcessor paymentDisbursementProcessor,
                                     PaymentDisbursementStepListener paymentDisbursementStepListener,
                                     PaymentDisbursementItemReadListener paymentDisbursementItemReadListener,
                                     PaymentDisbursementItemProcessListener paymentDisbursementItemProcessListener,
                                     PaymentDisbursementItemWriteListener paymentDisbursementItemWriteListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.paymentDisbursementProcessor = paymentDisbursementProcessor;
        this.paymentDisbursementStepListener = paymentDisbursementStepListener;
        this.paymentDisbursementItemReadListener = paymentDisbursementItemReadListener;
        this.paymentDisbursementItemProcessListener = paymentDisbursementItemProcessListener;
        this.paymentDisbursementItemWriteListener = paymentDisbursementItemWriteListener;
    }

    @Bean
    public Job paymentDisbursementJob() {
        return new JobBuilder(Constants.BatchJob.PAYMENT_DISBURSEMENT, jobRepository)
                .validator(new EntityIdJobParametersValidator())
                .start(paymentDisbursementStep())
                .build();
    }

    @Bean
    @StepScope
    public org.springframework.batch.item.file.FlatFileItemReader<EmployeeCSVDTO> paymentDisbursementReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        String filePath = inputFile != null ? inputFile : Constants.FilePaths.INPUT_DIR + Constants.FilePaths.EMPLOYEES_CSV;
        
        Resource resource;
        // Check if it's explicitly a classpath resource or an absolute file path
        if (filePath.startsWith("classpath:")) {
            String classpathFile = filePath.substring(10);
            resource = new ClassPathResource(classpathFile);
        } else if (filePath.startsWith("/") || (filePath.length() > 1 && filePath.charAt(1) == ':')) {
            // Absolute path (starts with / or drive letter like C:)
            resource = new FileSystemResource(filePath);
        } else {
            // Relative path - treat as classpath resource
            resource = new ClassPathResource(filePath);
        }

        return new org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder<EmployeeCSVDTO>()
                .name("paymentDisbursementReader")
                .resource(resource)
                .delimited()
                .names("id", "name", "department", "salary")
                .fieldSetMapper(new org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper<EmployeeCSVDTO>() {{
                    setTargetType(EmployeeCSVDTO.class);
                }})
                .linesToSkip(1)
                .build();
    }

    @Bean
    @StepScope
    public org.springframework.batch.item.file.FlatFileItemWriter<EmployeeCSVDTO> paymentDisbursementWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        String filePath = outputFile != null ? outputFile : Constants.FilePaths.DISBURSED_EMPLOYEES_CSV;
        String outputPath = java.nio.file.Paths.get(Constants.FilePaths.OUTPUT_DIR, filePath).toString();
        
        return new org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder<EmployeeCSVDTO>()
                .name("paymentDisbursementWriter")
                .resource(new org.springframework.core.io.FileSystemResource(outputPath))
                .delimited()
                .delimiter(",")
                .names("id", "name", "department", "salary")
                .headerCallback(writer -> writer.write("id,name,department,salary"))
                .build();
    }

    @Bean
    public Step paymentDisbursementStep() {
        return new StepBuilder("paymentDisbursementStep", jobRepository)
                .<EmployeeCSVDTO, EmployeeCSVDTO>chunk(Constants.BatchJob.DEFAULT_CHUNK_SIZE, transactionManager)
                .reader((org.springframework.batch.item.ItemReader<EmployeeCSVDTO>) applicationContext.getBean("paymentDisbursementReader"))
                .listener(paymentDisbursementItemReadListener)
                .processor(paymentDisbursementProcessor)
                .listener(paymentDisbursementItemProcessListener)
                .writer((org.springframework.batch.item.ItemWriter<EmployeeCSVDTO>) applicationContext.getBean("paymentDisbursementWriter"))
                .listener(paymentDisbursementItemWriteListener)
                .listener(paymentDisbursementStepListener)
                .build();
    }
}
