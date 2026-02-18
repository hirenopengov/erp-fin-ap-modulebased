package com.opengov.erp.ap.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing vendor address from external API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorAddressDTO {
    private Integer addressId;
    private Integer vendorKey;
    private String addressLine;
    private String streetAddress;
    private String city;
    private String state;
    private String zipcode;
    private String phoneNumber;
    
    /**
     * Gets formatted address string.
     *
     * @return Formatted address string
     */
    public String getFormattedAddress() {
        StringBuilder address = new StringBuilder();
        
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            address.append(streetAddress);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        
        if (zipcode != null && !zipcode.trim().isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(zipcode);
        }
        
        return address.toString();
    }
}

