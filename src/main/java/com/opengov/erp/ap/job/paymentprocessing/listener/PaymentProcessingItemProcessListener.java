package com.opengov.erp.ap.job.paymentprocessing.listener;

import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessingItemProcessListener implements ItemProcessListener<EmployeeCSVDTO, EmployeeCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingItemProcessListener.class);

    @Override
    public void beforeProcess(EmployeeCSVDTO item) {
        logger.debug("[Payment Processing - PROCESS] Starting to process employee record - ID: {}, Name: {}, Original Salary: {}",
                item.getId(), item.getName(), item.getSalary());
    }

    @Override
    public void afterProcess(EmployeeCSVDTO item, EmployeeCSVDTO result) {
        if (result != null) {
            logger.debug("[Payment Processing - PROCESS] Successfully processed employee record - ID: {}, Name: {}, Processed Salary: {}",
                    result.getId(), result.getName(), result.getSalary());
        }
    }

    @Override
    public void onProcessError(EmployeeCSVDTO item, Exception e) {
        logger.error("[Payment Processing - PROCESS] Error occurred while processing employee record - ID: {}, Name: {}, Error: {}",
                item != null ? item.getId() : "N/A",
                item != null ? item.getName() : "N/A",
                e.getMessage(), e);
    }
}
