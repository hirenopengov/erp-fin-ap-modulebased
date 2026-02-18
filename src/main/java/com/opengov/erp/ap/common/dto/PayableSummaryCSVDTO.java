package com.opengov.erp.ap.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayableSummaryCSVDTO {
    private Integer vendorId;
    private String vendorNumber;
    private Double totalPayableAmount;
    private Integer invoiceCount;
    private String referenceNumber;
    private String status;
    private Instant dateCreated;
    private String address;
}

