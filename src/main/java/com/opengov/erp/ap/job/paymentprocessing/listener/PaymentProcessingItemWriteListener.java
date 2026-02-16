package com.opengov.erp.ap.job.paymentprocessing.listener;

import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentProcessingItemWriteListener implements ItemWriteListener<EmployeeCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingItemWriteListener.class);

    @Override
    public void beforeWrite(List<? extends EmployeeCSVDTO> items) {
        logger.info("[Payment Processing - WRITE] Starting to write {} employee record(s) to output file", items.size());
        items.forEach(item -> 
            logger.debug("[Payment Processing - WRITE] Preparing to write - ID: {}, Name: {}, Salary: {}",
                    item.getId(), item.getName(), item.getSalary())
        );
    }

    @Override
    public void afterWrite(List<? extends EmployeeCSVDTO> items) {
        logger.info("[Payment Processing - WRITE] Successfully wrote {} employee record(s) to output file", items.size());
        items.forEach(item -> 
            logger.debug("[Payment Processing - WRITE] Written - ID: {}, Name: {}, Processed Salary: {}",
                    item.getId(), item.getName(), item.getSalary())
        );
    }

    @Override
    public void onWriteError(Exception exception, List<? extends EmployeeCSVDTO> items) {
        logger.error("[Payment Processing - WRITE] Error occurred while writing {} employee record(s). Error: {}",
                items.size(), exception.getMessage(), exception);
        items.forEach(item -> 
            logger.error("[Payment Processing - WRITE] Failed record - ID: {}, Name: {}",
                    item.getId(), item.getName())
        );
    }
}
