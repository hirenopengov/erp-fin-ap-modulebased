package com.opengov.erp.ap.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing vendor details from external API.
 * Matches the actual API response structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDTO {
    private Integer vendorKey;
    private String entityId;
    private String vendorId;
    private String legalCompanyName;
    private String doingBusinessAs;
    private String businessType;
    private String companyWebsite;
    private String companyDescription;
    private String externalSystemCode;
    private String status;
    private String vendorApprovalStatus;
    private String createdAt;
    private String updatedAt;
    private List<VendorAddressDTO> addresses;
    private List<VendorContactDTO> contacts;
    
    /**
     * Gets the primary address from addresses array.
     * Returns the first address if available, or empty string if no addresses.
     *
     * @return Formatted address string
     */
    public String getPrimaryAddress() {
        if (addresses == null || addresses.isEmpty()) {
            return "";
        }
        
        // Return the first address formatted
        VendorAddressDTO primaryAddress = addresses.get(0);
        return primaryAddress.getFormattedAddress();
    }
    
    /**
     * Gets address by addressLine if specified, otherwise returns primary address.
     *
     * @param addressLine The address line to search for (e.g., "REMIT TO")
     * @return Formatted address string
     */
    public String getAddressByLine(String addressLine) {
        if (addresses == null || addresses.isEmpty()) {
            return "";
        }
        
        if (addressLine != null && !addressLine.trim().isEmpty()) {
            for (VendorAddressDTO address : addresses) {
                if (addressLine.equalsIgnoreCase(address.getAddressLine())) {
                    return address.getFormattedAddress();
                }
            }
        }
        
        // Fallback to primary address
        return getPrimaryAddress();
    }
}
