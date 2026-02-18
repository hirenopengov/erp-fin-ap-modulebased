package com.opengov.erp.ap.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing vendor contact from external API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorContactDTO {
    private Integer contactId;
    private Integer vendorKey;
    private String salutation;
    private String name;
    private String title;
    private String comments;
    private String contactOrg;
    private String phone;
    private String phoneType;
    private String email;
    private String emailType;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private List<String> usageTypes;
    private String isDefault;
}

