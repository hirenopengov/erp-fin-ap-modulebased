package com.opengov.erp.ap.job.payablesummary.listener;

import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
public class PayableSummaryItemProcessListener implements ItemProcessListener<PayableSummaryCSVDTO, PayableSummaryCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PayableSummaryItemProcessListener.class);

    @Override
    public void beforeProcess(PayableSummaryCSVDTO item) {
        if (item != null) {
            logger.debug("[Payable Summary - PROCESS] Starting to process payable summary - VendorId: {}, TotalAmount: {}",
                    item.getVendorId(), item.getTotalPayableAmount());
        } else {
            logger.debug("[Payable Summary - PROCESS] Starting to process payable summary - Item is null");
        }
    }

    @Override
    public void afterProcess(PayableSummaryCSVDTO item, PayableSummaryCSVDTO result) {
        if (result != null) {
            logger.debug("[Payable Summary - PROCESS] Successfully processed payable summary - VendorId: {}, ReferenceNumber: {}, Status: {}",
                    result.getVendorId(), result.getReferenceNumber(), result.getStatus());
        }
    }

    @Override
    public void onProcessError(PayableSummaryCSVDTO item, Exception e) {
        logger.error("[Payable Summary - PROCESS] Error occurred while processing payable summary - VendorId: {}, Error: {}",
                item != null ? item.getVendorId() : "N/A",
                e.getMessage(), e);
    }
}

