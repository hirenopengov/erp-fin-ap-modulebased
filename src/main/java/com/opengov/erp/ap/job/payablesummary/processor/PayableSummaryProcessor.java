package com.opengov.erp.ap.job.payablesummary.processor;

import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import com.opengov.erp.ap.common.service.VendorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@StepScope
public class PayableSummaryProcessor implements ItemProcessor<PayableSummaryCSVDTO, PayableSummaryCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PayableSummaryProcessor.class);

    private final VendorService vendorService;
    
    @Value("#{jobParameters['entityId']}")
    private String entityId;

    public PayableSummaryProcessor(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @Override
    public PayableSummaryCSVDTO process(PayableSummaryCSVDTO item) throws Exception {
        // Add output fields: referenceNumber, status, dateCreated, address
        PayableSummaryCSVDTO processed = new PayableSummaryCSVDTO();
        processed.setVendorId(item.getVendorId());
        processed.setVendorNumber(item.getVendorNumber());
        processed.setTotalPayableAmount(item.getTotalPayableAmount());
        processed.setInvoiceCount(item.getInvoiceCount());
        
        // Generate unique reference number per vendor row
        processed.setReferenceNumber("VENDOR-" + item.getVendorId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Fixed status value
        processed.setStatus("DRAFT");
        
        // Timestamp when file is generated (UTC now)
        processed.setDateCreated(Instant.now());
        
        // Check if vendor exists and fetch address from external API
        if (item.getVendorId() != null && entityId != null && !entityId.trim().isEmpty()) {
            // vendorId from database is the vendorKey (integer) used in API
            Integer vendorKey = item.getVendorId();
            
            boolean vendorExists = vendorService.vendorExists(entityId, vendorKey);
            logger.debug("Vendor existence check: entityId={}, vendorKey={}, exists={}", entityId, vendorKey, vendorExists);
            
            if (vendorExists) {
                Optional<String> addressOptional = vendorService.getVendorAddress(entityId, vendorKey);
                if (addressOptional.isPresent() && !addressOptional.get().trim().isEmpty()) {
                    processed.setAddress(addressOptional.get());
                    logger.debug("Vendor address fetched: entityId={}, vendorKey={}, address={}", entityId, vendorKey, addressOptional.get());
                } else {
                    logger.warn("Vendor exists but address not found: entityId={}, vendorKey={}", entityId, vendorKey);
                    processed.setAddress("");
                }
            } else {
                logger.warn("Vendor does not exist: entityId={}, vendorKey={}", entityId, vendorKey);
                processed.setAddress("");
            }
        } else {
            if (item.getVendorId() == null) {
                logger.warn("Vendor ID is null, skipping vendor check");
            }
            if (entityId == null || entityId.trim().isEmpty()) {
                logger.warn("Entity ID is null or empty, skipping vendor check");
            }
            processed.setAddress("");
        }
        
        return processed;
    }
}

