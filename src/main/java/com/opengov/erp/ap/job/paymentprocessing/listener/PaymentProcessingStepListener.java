package com.opengov.erp.ap.job.paymentprocessing.listener;

import com.opengov.erp.ap.common.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessingStepListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Set tenant context from job parameters
        String entityId = stepExecution.getJobParameters().getString("entityId");
        if (entityId != null && !entityId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(entityId);
            logger.info("[Payment Processing - STEP START] Tenant context set to entity_id: {}", entityId);
        } else {
            logger.warn("[Payment Processing - STEP START] No entity_id parameter provided. Tenant context not set.");
        }
        logger.info("========================================");
        logger.info("[Payment Processing - STEP START] Payment Processing Step Started");
        logger.info("[Payment Processing - STEP START] Step Name: {}", stepExecution.getStepName());
        logger.info("[Payment Processing - STEP START] Job Name: {}", stepExecution.getJobExecution().getJobInstance().getJobName());
        logger.info("[Payment Processing - STEP START] Job Execution ID: {}", stepExecution.getJobExecution().getId());
        logger.info("[Payment Processing - STEP START] Job Parameters: {}", stepExecution.getJobParameters());
        
        // Log custom parameters if present
        String bonusPercentage = stepExecution.getJobParameters().getString("bonusPercentage");
        String inputFile = stepExecution.getJobParameters().getString("inputFile");
        String outputFile = stepExecution.getJobParameters().getString("outputFile");
        
        if (bonusPercentage != null) {
            logger.info("[Payment Processing - STEP START] Bonus Percentage: {}%", bonusPercentage);
        } else {
            logger.info("[Payment Processing - STEP START] Bonus Percentage: 10.0% (default)");
        }
        if (inputFile != null) {
            logger.info("[Payment Processing - STEP START] Input File: {}", inputFile);
        } else {
            logger.info("[Payment Processing - STEP START] Input File: {} (default)", 
                    "data/input/employees.csv");
        }
        if (outputFile != null) {
            logger.info("[Payment Processing - STEP START] Output File: {}", outputFile);
        } else {
            logger.info("[Payment Processing - STEP START] Output File: {} (default)", 
                    "processed_employees.csv");
        }
        
        logger.info("========================================");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("========================================");
        logger.info("[Payment Processing - STEP END] Payment Processing Step Completed");
        logger.info("[Payment Processing - STEP END] Step Name: {}", stepExecution.getStepName());
        logger.info("[Payment Processing - STEP END] Exit Status: {}", stepExecution.getExitStatus());
        logger.info("[Payment Processing - STEP END] Statistics:");
        logger.info("[Payment Processing - STEP END]   - Total Records Read: {}", stepExecution.getReadCount());
        logger.info("[Payment Processing - STEP END]   - Total Records Written: {}", stepExecution.getWriteCount());
        logger.info("[Payment Processing - STEP END]   - Records Filtered: {}", stepExecution.getFilterCount());
        logger.info("[Payment Processing - STEP END]   - Records Skipped: {}", stepExecution.getSkipCount());
        logger.info("[Payment Processing - STEP END]   - Commits: {}", stepExecution.getCommitCount());
        logger.info("[Payment Processing - STEP END]   - Rollbacks: {}", stepExecution.getRollbackCount());
        
        long duration = stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime();
        logger.info("[Payment Processing - STEP END] Execution Duration: {} ms ({} seconds)", 
                duration, String.format("%.2f", duration / 1000.0));
        logger.info("========================================");
        
        // Clear tenant context after step completion
        TenantContext.clear();
        
        return stepExecution.getExitStatus();
    }
}
