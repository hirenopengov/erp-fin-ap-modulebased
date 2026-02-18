package com.opengov.erp.ap.job.payablesummary.listener;

import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PayableSummaryItemWriteListener implements ItemWriteListener<PayableSummaryCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PayableSummaryItemWriteListener.class);

    public void beforeWrite(List<? extends PayableSummaryCSVDTO> items) {
        logger.info("[Payable Summary - WRITE] Starting to write {} payable summary record(s) to CSV file", items.size());
        items.forEach(item -> 
            logger.debug("[Payable Summary - WRITE] Preparing to write - VendorId: {}, VendorNumber: {}, TotalAmount: {}, InvoiceCount: {}",
                    item.getVendorId(), item.getVendorNumber(), item.getTotalPayableAmount(), item.getInvoiceCount())
        );
    }

    public void afterWrite(List<? extends PayableSummaryCSVDTO> items) {
        logger.info("[Payable Summary - WRITE] Successfully wrote {} payable summary record(s) to CSV file", items.size());
        items.forEach(item -> 
            logger.debug("[Payable Summary - WRITE] Written - VendorId: {}, ReferenceNumber: {}, Status: {}",
                    item.getVendorId(), item.getReferenceNumber(), item.getStatus())
        );
    }

    public void onWriteError(Exception exception, List<? extends PayableSummaryCSVDTO> items) {
        logger.error("[Payable Summary - WRITE] Error occurred while writing {} payable summary record(s). Error: {}",
                items.size(), exception.getMessage(), exception);
        items.forEach(item -> 
            logger.error("[Payable Summary - WRITE] Failed record - VendorId: {}, VendorNumber: {}",
                    item.getVendorId(), item.getVendorNumber())
        );
    }
}

