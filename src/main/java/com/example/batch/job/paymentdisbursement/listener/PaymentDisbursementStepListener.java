package com.opengov.erp.ap.job.paymentdisbursement.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentDisbursementStepListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDisbursementStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("=== Payment Disbursement Step Started ===");
        logger.info("Step Name: {}", stepExecution.getStepName());
        logger.info("Job Name: {}", stepExecution.getJobExecution().getJobInstance().getJobName());
        logger.info("Job Parameters: {}", stepExecution.getJobParameters());
        logger.info("Job Execution ID: {}", stepExecution.getJobExecution().getId());
        
        // Log custom parameters if present
        String taxRate = stepExecution.getJobParameters().getString("taxRate");
        String inputFile = stepExecution.getJobParameters().getString("inputFile");
        String outputFile = stepExecution.getJobParameters().getString("outputFile");
        
        if (taxRate != null) {
            logger.info("Tax Rate: {}%", taxRate);
        }
        if (inputFile != null) {
            logger.info("Input File: {}", inputFile);
        }
        if (outputFile != null) {
            logger.info("Output File: {}", outputFile);
        }
        
        logger.info("==========================================");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("=== Payment Disbursement Step Completed ===");
        logger.info("Step Name: {}", stepExecution.getStepName());
        logger.info("Read Count: {}", stepExecution.getReadCount());
        logger.info("Write Count: {}", stepExecution.getWriteCount());
        logger.info("Filter Count: {}", stepExecution.getFilterCount());
        logger.info("Skip Count: {}", stepExecution.getSkipCount());
        logger.info("Commit Count: {}", stepExecution.getCommitCount());
        logger.info("Rollback Count: {}", stepExecution.getRollbackCount());
        logger.info("Exit Status: {}", stepExecution.getExitStatus());
        logger.info("Duration: {} ms", stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime());
        logger.info("=============================================");
        
        return stepExecution.getExitStatus();
    }
}
