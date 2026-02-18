package com.opengov.erp.ap.job.payablesummary.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.context.TenantContext;
import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import com.opengov.erp.ap.common.service.PaymentRunWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class PayableSummaryStepListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(PayableSummaryStepListener.class);

    private final PaymentRunWorkflowService paymentRunWorkflowService;
    private final ObjectMapper objectMapper;

    public PayableSummaryStepListener(PaymentRunWorkflowService paymentRunWorkflowService) {
        this.paymentRunWorkflowService = paymentRunWorkflowService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Set tenant context from job parameters
        String entityId = stepExecution.getJobParameters().getString("entityId");
        if (entityId != null && !entityId.trim().isEmpty()) {
            TenantContext.setCurrentTenant(entityId);
            logger.info("[Payable Summary - STEP START] Tenant context set to entity_id: {}", entityId);
        } else {
            logger.warn("[Payable Summary - STEP START] No entity_id parameter provided. Tenant context not set.");
        }
        logger.info("========================================");
        logger.info("[Payable Summary - STEP START] Payable Summary Step Started");
        logger.info("[Payable Summary - STEP START] Step Name: {}", stepExecution.getStepName());
        logger.info("[Payable Summary - STEP START] Job Name: {}", stepExecution.getJobExecution().getJobInstance().getJobName());
        logger.info("[Payable Summary - STEP START] Job Execution ID: {}", stepExecution.getJobExecution().getId());
        logger.info("[Payable Summary - STEP START] Job Parameters: {}", stepExecution.getJobParameters());
        
        // Log custom parameters if present
        String paymentRunId = getParameterAsString(stepExecution, "paymentRunId");
        
        if (paymentRunId != null) {
            logger.info("[Payable Summary - STEP START] Payment Run ID: {}", paymentRunId);
        }
        logger.info("[Payable Summary - STEP START] Output Files: PayableSummary.csv, PayableSummary.json");
        
        logger.info("========================================");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("========================================");
        logger.info("[Payable Summary - STEP END] Payable Summary Step Completed");
        logger.info("[Payable Summary - STEP END] Step Name: {}", stepExecution.getStepName());
        logger.info("[Payable Summary - STEP END] Exit Status: {}", stepExecution.getExitStatus());
        logger.info("[Payable Summary - STEP END] Statistics:");
        logger.info("[Payable Summary - STEP END]   - Total Records Read: {}", stepExecution.getReadCount());
        logger.info("[Payable Summary - STEP END]   - Total Records Written: {}", stepExecution.getWriteCount());
        logger.info("[Payable Summary - STEP END]   - Records Filtered: {}", stepExecution.getFilterCount());
        logger.info("[Payable Summary - STEP END]   - Records Skipped: {}", stepExecution.getSkipCount());
        logger.info("[Payable Summary - STEP END]   - Commits: {}", stepExecution.getCommitCount());
        logger.info("[Payable Summary - STEP END]   - Rollbacks: {}", stepExecution.getRollbackCount());
        
        LocalDateTime startTime = stepExecution.getStartTime();
        LocalDateTime endTime = stepExecution.getEndTime();
        if (startTime != null && endTime != null) {
            long duration = Duration.between(startTime, endTime).toMillis();
            logger.info("[Payable Summary - STEP END] Execution Duration: {} ms ({} seconds)", 
                    duration, String.format("%.2f", duration / 1000.0));
        } else {
            logger.warn("[Payable Summary - STEP END] Execution Duration: Unable to calculate (startTime or endTime is null)");
        }
        logger.info("========================================");
        
        // Update config_snapshot in pr_ap_payment_run_workflow table
        if (stepExecution.getExitStatus().getExitCode().equals(ExitStatus.COMPLETED.getExitCode())) {
            updateConfigSnapshot(stepExecution);
        }
        
        // Clear tenant context after step completion
        TenantContext.clear();
        
        return stepExecution.getExitStatus();
    }

    /**
     * Updates the config_snapshot column in pr_ap_payment_run_workflow table with JSON data.
     */
    private void updateConfigSnapshot(StepExecution stepExecution) {
        try {
            String paymentRunIdStr = getParameterAsString(stepExecution, "paymentRunId");
            String entityIdStr = getParameterAsString(stepExecution, "entityId");
            
            if (paymentRunIdStr == null || entityIdStr == null) {
                logger.warn("[Payable Summary - CONFIG UPDATE] Missing paymentRunId or entityId. Skipping config_snapshot update.");
                return;
            }
            
            UUID paymentRunId = UUID.fromString(paymentRunIdStr);
            UUID entityId = UUID.fromString(entityIdStr);
            
            // Read JSON file that was created by the JSON writer
            String jsonFilePath = Paths.get(Constants.FilePaths.OUTPUT_DIR, "PayableSummary.json").toString();
            File jsonFile = new File(jsonFilePath);
            
            if (!jsonFile.exists()) {
                logger.warn("[Payable Summary - CONFIG UPDATE] JSON file not found: {}. Skipping config_snapshot update.", jsonFilePath);
                return;
            }
            
            // Read JSON content from file
            List<PayableSummaryCSVDTO> items = objectMapper.readValue(
                jsonFile, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, PayableSummaryCSVDTO.class)
            );
            
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(items);
            
            logger.info("[Payable Summary - CONFIG UPDATE] Read JSON file with {} items. Updating config_snapshot for paymentRunId={}, entityId={}", 
                items.size(), paymentRunId, entityId);
            
            boolean success = paymentRunWorkflowService.updateConfigSnapshot(paymentRunId, entityId, jsonString);
            
            if (success) {
                logger.info("[Payable Summary - CONFIG UPDATE] Successfully updated config_snapshot");
            } else {
                logger.error("[Payable Summary - CONFIG UPDATE] Failed to update config_snapshot");
            }
        } catch (Exception e) {
            logger.error("[Payable Summary - CONFIG UPDATE] Error updating config_snapshot: {}", e.getMessage(), e);
        }
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

