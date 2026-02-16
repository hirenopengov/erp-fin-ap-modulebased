package com.opengov.erp.ap.job.paymentdisbursement.config;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementItemProcessListener;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementItemReadListener;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementItemWriteListener;
import com.opengov.erp.ap.job.paymentdisbursement.listener.PaymentDisbursementStepListener;
import com.opengov.erp.ap.job.paymentdisbursement.reader.PaymentDisbursementReader;
import com.opengov.erp.ap.job.paymentdisbursement.processor.PaymentDisbursementProcessor;
import com.opengov.erp.ap.job.paymentdisbursement.writer.PaymentDisbursementWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PaymentDisbursementConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PaymentDisbursementReader paymentDisbursementReader;
    private final PaymentDisbursementProcessor paymentDisbursementProcessor;
    private final PaymentDisbursementWriter paymentDisbursementWriter;
    private final PaymentDisbursementStepListener paymentDisbursementStepListener;
    private final PaymentDisbursementItemReadListener paymentDisbursementItemReadListener;
    private final PaymentDisbursementItemProcessListener paymentDisbursementItemProcessListener;
    private final PaymentDisbursementItemWriteListener paymentDisbursementItemWriteListener;

    public PaymentDisbursementConfig(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     PaymentDisbursementReader paymentDisbursementReader,
                                     PaymentDisbursementProcessor paymentDisbursementProcessor,
                                     PaymentDisbursementWriter paymentDisbursementWriter,
                                     PaymentDisbursementStepListener paymentDisbursementStepListener,
                                     PaymentDisbursementItemReadListener paymentDisbursementItemReadListener,
                                     PaymentDisbursementItemProcessListener paymentDisbursementItemProcessListener,
                                     PaymentDisbursementItemWriteListener paymentDisbursementItemWriteListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.paymentDisbursementReader = paymentDisbursementReader;
        this.paymentDisbursementProcessor = paymentDisbursementProcessor;
        this.paymentDisbursementWriter = paymentDisbursementWriter;
        this.paymentDisbursementStepListener = paymentDisbursementStepListener;
        this.paymentDisbursementItemReadListener = paymentDisbursementItemReadListener;
        this.paymentDisbursementItemProcessListener = paymentDisbursementItemProcessListener;
        this.paymentDisbursementItemWriteListener = paymentDisbursementItemWriteListener;
    }

    @Bean
    public Job paymentDisbursementJob() {
        return new JobBuilder(Constants.BatchJob.PAYMENT_DISBURSEMENT, jobRepository)
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("paymentDisbursementStep", jobRepository)
                .<EmployeeCSVDTO, EmployeeCSVDTO>chunk(Constants.BatchJob.DEFAULT_CHUNK_SIZE, transactionManager)
                .reader(paymentDisbursementReader.reader())
                .listener(paymentDisbursementItemReadListener)
                .processor(paymentDisbursementProcessor)
                .listener(paymentDisbursementItemProcessListener)
                .writer(paymentDisbursementWriter.writer())
                .listener(paymentDisbursementItemWriteListener)
                .listener(paymentDisbursementStepListener)
                .build();
    }
}
