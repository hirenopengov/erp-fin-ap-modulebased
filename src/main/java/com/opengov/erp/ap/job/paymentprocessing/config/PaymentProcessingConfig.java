package com.opengov.erp.ap.job.paymentprocessing.config;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import com.opengov.erp.ap.common.validator.EntityIdJobParametersValidator;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingItemProcessListener;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingItemReadListener;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingItemWriteListener;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingStepListener;
import com.opengov.erp.ap.job.paymentprocessing.processor.PaymentProcessingProcessor;
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
public class PaymentProcessingConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PaymentProcessingProcessor paymentProcessingProcessor;
    private final PaymentProcessingStepListener paymentProcessingStepListener;
    private final PaymentProcessingItemReadListener paymentProcessingItemReadListener;
    private final PaymentProcessingItemProcessListener paymentProcessingItemProcessListener;
    private final PaymentProcessingItemWriteListener paymentProcessingItemWriteListener;
    
    @Autowired
    private ApplicationContext applicationContext;

    public PaymentProcessingConfig(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   PaymentProcessingProcessor paymentProcessingProcessor,
                                   PaymentProcessingStepListener paymentProcessingStepListener,
                                   PaymentProcessingItemReadListener paymentProcessingItemReadListener,
                                   PaymentProcessingItemProcessListener paymentProcessingItemProcessListener,
                                   PaymentProcessingItemWriteListener paymentProcessingItemWriteListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.paymentProcessingProcessor = paymentProcessingProcessor;
        this.paymentProcessingStepListener = paymentProcessingStepListener;
        this.paymentProcessingItemReadListener = paymentProcessingItemReadListener;
        this.paymentProcessingItemProcessListener = paymentProcessingItemProcessListener;
        this.paymentProcessingItemWriteListener = paymentProcessingItemWriteListener;
    }

    @Bean
    public Job paymentProcessingJob() {
        return new JobBuilder(Constants.BatchJob.PAYMENT_PROCESSING, jobRepository)
                .validator(new EntityIdJobParametersValidator())
                .start(paymentProcessingStep())
                .build();
    }

    @Bean
    @StepScope
    public org.springframework.batch.item.file.FlatFileItemReader<EmployeeCSVDTO> paymentProcessingReader(
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
                .name("paymentProcessingReader")
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
    public org.springframework.batch.item.file.FlatFileItemWriter<EmployeeCSVDTO> paymentProcessingWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        String filePath = outputFile != null ? outputFile : Constants.FilePaths.PROCESSED_EMPLOYEES_CSV;
        String outputPath = java.nio.file.Paths.get(Constants.FilePaths.OUTPUT_DIR, filePath).toString();
        
        return new org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder<EmployeeCSVDTO>()
                .name("paymentProcessingWriter")
                .resource(new org.springframework.core.io.FileSystemResource(outputPath))
                .delimited()
                .delimiter(",")
                .names("id", "name", "department", "salary")
                .headerCallback(writer -> writer.write("id,name,department,salary"))
                .build();
    }

    @Bean
    public Step paymentProcessingStep() {
        return new StepBuilder("paymentProcessingStep", jobRepository)
                .<EmployeeCSVDTO, EmployeeCSVDTO>chunk(Constants.BatchJob.DEFAULT_CHUNK_SIZE, transactionManager)
                .reader((org.springframework.batch.item.ItemReader<EmployeeCSVDTO>) applicationContext.getBean("paymentProcessingReader"))
                .listener(paymentProcessingItemReadListener)
                .processor(paymentProcessingProcessor)
                .listener(paymentProcessingItemProcessListener)
                .writer((org.springframework.batch.item.ItemWriter<EmployeeCSVDTO>) applicationContext.getBean("paymentProcessingWriter"))
                .listener(paymentProcessingItemWriteListener)
                .listener(paymentProcessingStepListener)
                .build();
    }
}
