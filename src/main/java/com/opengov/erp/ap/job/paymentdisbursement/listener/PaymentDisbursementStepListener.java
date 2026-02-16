package com.opengov.erp.ap.job.paymentdisbursement.listener;

import com.opengov.erp.ap.common.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class PaymentDisbursementStepListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDisbursementStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Set tenant context from job parameters
        String entityId = stepExecution.getJobParameters().getString("entityId");
        if (entityId != null && !entityId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(entityId);
            logger.info("[Payment Disbursement - STEP START] Tenant context set to entity_id: {}", entityId);
        } else {
            logger.warn("[Payment Disbursement - STEP START] No entity_id parameter provided. Tenant context not set.");
        }
        logger.info("========================================");
        logger.info("[Payment Disbursement - STEP START] Payment Disbursement Step Started");
        logger.info("[Payment Disbursement - STEP START] Step Name: {}", stepExecution.getStepName());
        logger.info("[Payment Disbursement - STEP START] Job Name: {}", stepExecution.getJobExecution().getJobInstance().getJobName());
        logger.info("[Payment Disbursement - STEP START] Job Execution ID: {}", stepExecution.getJobExecution().getId());
        logger.info("[Payment Disbursement - STEP START] Job Parameters: {}", stepExecution.getJobParameters());
        
        // Log custom parameters if present
        String taxRate = getParameterAsString(stepExecution, "taxRate");
        String inputFile = getParameterAsString(stepExecution, "inputFile");
        String outputFile = getParameterAsString(stepExecution, "outputFile");
        
        if (taxRate != null) {
            logger.info("[Payment Disbursement - STEP START] Tax Rate: {}%", taxRate);
        } else {
            logger.info("[Payment Disbursement - STEP START] Tax Rate: 5.0% (default)");
        }
        if (inputFile != null) {
            logger.info("[Payment Disbursement - STEP START] Input File: {}", inputFile);
        } else {
            logger.info("[Payment Disbursement - STEP START] Input File: {} (default)", 
                    "data/input/employees.csv");
        }
        if (outputFile != null) {
            logger.info("[Payment Disbursement - STEP START] Output File: {}", outputFile);
        } else {
            logger.info("[Payment Disbursement - STEP START] Output File: {} (default)", 
                    "disbursed_employees.csv");
        }
        
        logger.info("========================================");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("========================================");
        logger.info("[Payment Disbursement - STEP END] Payment Disbursement Step Completed");
        logger.info("[Payment Disbursement - STEP END] Step Name: {}", stepExecution.getStepName());
        logger.info("[Payment Disbursement - STEP END] Exit Status: {}", stepExecution.getExitStatus());
        logger.info("[Payment Disbursement - STEP END] Statistics:");
        logger.info("[Payment Disbursement - STEP END]   - Total Records Read: {}", stepExecution.getReadCount());
        logger.info("[Payment Disbursement - STEP END]   - Total Records Written: {}", stepExecution.getWriteCount());
        logger.info("[Payment Disbursement - STEP END]   - Records Filtered: {}", stepExecution.getFilterCount());
        logger.info("[Payment Disbursement - STEP END]   - Records Skipped: {}", stepExecution.getSkipCount());
        logger.info("[Payment Disbursement - STEP END]   - Commits: {}", stepExecution.getCommitCount());
        logger.info("[Payment Disbursement - STEP END]   - Rollbacks: {}", stepExecution.getRollbackCount());
        
        LocalDateTime startTime = stepExecution.getStartTime();
        LocalDateTime endTime = stepExecution.getEndTime();
        if (startTime != null && endTime != null) {
            long duration = Duration.between(startTime, endTime).toMillis();
            logger.info("[Payment Disbursement - STEP END] Execution Duration: {} ms ({} seconds)", 
                    duration, String.format("%.2f", duration / 1000.0));
        } else {
            logger.warn("[Payment Disbursement - STEP END] Execution Duration: Unable to calculate (startTime or endTime is null)");
        }
        logger.info("========================================");
        
        // Clear tenant context after step completion
        TenantContext.clear();
        
        return stepExecution.getExitStatus();
    }
    
    private String getParameterAsString(StepExecution stepExecution, String key) {
        org.springframework.batch.core.JobParameters params = stepExecution.getJobParameters();
        try {
            String value = params.getString(key);
            if (value != null) {
                return value;
            }
        } catch (IllegalArgumentException e) {
            // Parameter exists but is not a String type
        }
        
        try {
            Long longValue = params.getLong(key);
            if (longValue != null) {
                return String.valueOf(longValue);
            }
        } catch (IllegalArgumentException e) {
            // Parameter exists but is not a Long type
        }
        
        try {
            Double doubleValue = params.getDouble(key);
            if (doubleValue != null) {
                return String.valueOf(doubleValue);
            }
        } catch (IllegalArgumentException e) {
            // Parameter exists but is not a Double type
        }
        
        try {
            java.util.Date dateValue = params.getDate(key);
            if (dateValue != null) {
                return String.valueOf(dateValue);
            }
        } catch (IllegalArgumentException e) {
            // Parameter exists but is not a Date type
        }
        
        return null;
    }
}
