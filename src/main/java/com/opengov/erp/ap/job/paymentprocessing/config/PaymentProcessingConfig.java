package com.opengov.erp.ap.job.paymentprocessing.config;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingItemProcessListener;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingItemReadListener;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingItemWriteListener;
import com.opengov.erp.ap.job.paymentprocessing.listener.PaymentProcessingStepListener;
import com.opengov.erp.ap.job.paymentprocessing.reader.PaymentProcessingReader;
import com.opengov.erp.ap.job.paymentprocessing.processor.PaymentProcessingProcessor;
import com.opengov.erp.ap.job.paymentprocessing.writer.PaymentProcessingWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PaymentProcessingConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PaymentProcessingReader paymentProcessingReader;
    private final PaymentProcessingProcessor paymentProcessingProcessor;
    private final PaymentProcessingWriter paymentProcessingWriter;
    private final PaymentProcessingStepListener paymentProcessingStepListener;
    private final PaymentProcessingItemReadListener paymentProcessingItemReadListener;
    private final PaymentProcessingItemProcessListener paymentProcessingItemProcessListener;
    private final PaymentProcessingItemWriteListener paymentProcessingItemWriteListener;

    public PaymentProcessingConfig(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   PaymentProcessingReader paymentProcessingReader,
                                   PaymentProcessingProcessor paymentProcessingProcessor,
                                   PaymentProcessingWriter paymentProcessingWriter,
                                   PaymentProcessingStepListener paymentProcessingStepListener,
                                   PaymentProcessingItemReadListener paymentProcessingItemReadListener,
                                   PaymentProcessingItemProcessListener paymentProcessingItemProcessListener,
                                   PaymentProcessingItemWriteListener paymentProcessingItemWriteListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.paymentProcessingReader = paymentProcessingReader;
        this.paymentProcessingProcessor = paymentProcessingProcessor;
        this.paymentProcessingWriter = paymentProcessingWriter;
        this.paymentProcessingStepListener = paymentProcessingStepListener;
        this.paymentProcessingItemReadListener = paymentProcessingItemReadListener;
        this.paymentProcessingItemProcessListener = paymentProcessingItemProcessListener;
        this.paymentProcessingItemWriteListener = paymentProcessingItemWriteListener;
    }

    @Bean
    public Job paymentProcessingJob() {
        return new JobBuilder(Constants.BatchJob.PAYMENT_PROCESSING, jobRepository)
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("paymentProcessingStep", jobRepository)
                .<EmployeeCSVDTO, EmployeeCSVDTO>chunk(Constants.BatchJob.DEFAULT_CHUNK_SIZE, transactionManager)
                .reader(paymentProcessingReader.reader())
                .listener(paymentProcessingItemReadListener)
                .processor(paymentProcessingProcessor)
                .listener(paymentProcessingItemProcessListener)
                .writer(paymentProcessingWriter.writer())
                .listener(paymentProcessingItemWriteListener)
                .listener(paymentProcessingStepListener)
                .build();
    }
}
