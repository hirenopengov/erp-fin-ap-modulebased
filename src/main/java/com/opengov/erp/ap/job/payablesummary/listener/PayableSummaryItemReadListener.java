package com.opengov.erp.ap.job.payablesummary.listener;

import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@Component
public class PayableSummaryItemReadListener implements ItemReadListener<PayableSummaryCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PayableSummaryItemReadListener.class);

    @Override
    public void beforeRead() {
        logger.debug("[Payable Summary - READ] Starting to read payable summary record...");
    }

    @Override
    public void afterRead(PayableSummaryCSVDTO item) {
        if (item != null) {
            logger.debug("[Payable Summary - READ] Successfully read payable summary - VendorId: {}, VendorNumber: {}, TotalAmount: {}, InvoiceCount: {}",
                    item.getVendorId(), item.getVendorNumber(), item.getTotalPayableAmount(), item.getInvoiceCount());
        }
    }

    @Override
    public void onReadError(Exception ex) {
        logger.error("[Payable Summary - READ] Error occurred while reading payable summary record: {}", ex.getMessage(), ex);
    }
}

