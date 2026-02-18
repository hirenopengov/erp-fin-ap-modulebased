package com.opengov.erp.ap.common.service;

import com.opengov.erp.ap.common.repository.PaymentRunWorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for payment run workflow operations.
 */
@Service
public class PaymentRunWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRunWorkflowService.class);

    private final PaymentRunWorkflowRepository paymentRunWorkflowRepository;

    public PaymentRunWorkflowService(PaymentRunWorkflowRepository paymentRunWorkflowRepository) {
        this.paymentRunWorkflowRepository = paymentRunWorkflowRepository;
    }

    /**
     * Updates the config_snapshot column in pr_ap_payment_run_workflow table.
     *
     * @param paymentRunId The payment run ID
     * @param entityId The entity ID
     * @param configSnapshotJson The JSON string to store in config_snapshot
     * @return true if update was successful, false otherwise
     */
    @Transactional
    public boolean updateConfigSnapshot(UUID paymentRunId, UUID entityId, String configSnapshotJson) {
        try {
            logger.info("Updating config_snapshot for paymentRunId={}, entityId={}", paymentRunId, entityId);
            
            int rowsUpdated = paymentRunWorkflowRepository.updateConfigSnapshot(
                paymentRunId, 
                entityId, 
                configSnapshotJson
            );
            
            if (rowsUpdated > 0) {
                logger.info("Successfully updated config_snapshot. Rows updated: {}", rowsUpdated);
                return true;
            } else {
                logger.warn("No rows updated. Payment run may not exist: paymentRunId={}, entityId={}", 
                    paymentRunId, entityId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error updating config_snapshot for paymentRunId={}, entityId={}: {}", 
                paymentRunId, entityId, e.getMessage(), e);
            return false;
        }
    }
}

