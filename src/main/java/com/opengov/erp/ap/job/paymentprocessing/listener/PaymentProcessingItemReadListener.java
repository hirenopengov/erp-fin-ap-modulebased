package com.opengov.erp.ap.job.paymentprocessing.listener;

import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessingItemReadListener implements ItemReadListener<EmployeeCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingItemReadListener.class);

    @Override
    public void beforeRead() {
        logger.debug("[Payment Processing - READ] Starting to read employee record...");
    }

    @Override
    public void afterRead(EmployeeCSVDTO item) {
        if (item != null) {
            logger.debug("[Payment Processing - READ] Successfully read employee record - ID: {}, Name: {}, Department: {}, Salary: {}",
                    item.getId(), item.getName(), item.getDepartment(), item.getSalary());
        }
    }

    @Override
    public void onReadError(Exception ex) {
        logger.error("[Payment Processing - READ] Error occurred while reading employee record: {}", ex.getMessage(), ex);
    }
}
