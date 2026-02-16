package com.opengov.erp.ap.job.paymentdisbursement.listener;

import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentDisbursementItemWriteListener implements ItemWriteListener<EmployeeCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDisbursementItemWriteListener.class);

    public void beforeWrite(List<? extends EmployeeCSVDTO> items) {
        logger.info("[Payment Disbursement - WRITE] Starting to write {} employee record(s) to output file", items.size());
        items.forEach(item -> 
            logger.debug("[Payment Disbursement - WRITE] Preparing to write - ID: {}, Name: {}, Net Salary: {}",
                    item.getId(), item.getName(), item.getSalary())
        );
    }

    public void afterWrite(List<? extends EmployeeCSVDTO> items) {
        logger.info("[Payment Disbursement - WRITE] Successfully wrote {} employee record(s) to output file", items.size());
        items.forEach(item -> 
            logger.debug("[Payment Disbursement - WRITE] Written - ID: {}, Name: {}, Net Salary: {}",
                    item.getId(), item.getName(), item.getSalary())
        );
    }

    public void onWriteError(Exception exception, List<? extends EmployeeCSVDTO> items) {
        logger.error("[Payment Disbursement - WRITE] Error occurred while writing {} employee record(s). Error: {}",
                items.size(), exception.getMessage(), exception);
        items.forEach(item -> 
            logger.error("[Payment Disbursement - WRITE] Failed record - ID: {}, Name: {}",
                    item.getId(), item.getName())
        );
    }
}
