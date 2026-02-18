package com.opengov.erp.ap.common.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for pr_ap_payment_run_workflow table operations.
 */
@Repository
public class PaymentRunWorkflowRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRunWorkflowRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Updates the config_snapshot column for a specific payment run.
     *
     * @param paymentRunId The payment run ID
     * @param entityId The entity ID
     * @param configSnapshot The JSON snapshot to store
     * @return Number of rows updated
     */
    public int updateConfigSnapshot(UUID paymentRunId, UUID entityId, String configSnapshot) {
        String sql = "UPDATE pr_ap_payment_run_workflow " +
                     "SET config_snapshot = CAST(? AS jsonb), " +
                     "    updated_at = now() " +
                     "WHERE id = ? AND entity_id = ?";
        
        return jdbcTemplate.update(sql, configSnapshot, paymentRunId, entityId);
    }
}

