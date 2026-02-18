package com.opengov.erp.ap.common.service;

import com.opengov.erp.ap.common.client.RestClientService;
import com.opengov.erp.ap.common.dto.VendorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Service for vendor-related operations including checking vendor existence
 * and fetching vendor details from external API.
 * 
 * API Endpoint: GET /api/v1/entities/:entityId/vendors/:id
 */
@Service
public class VendorService {

    private static final Logger logger = LoggerFactory.getLogger(VendorService.class);

    private final RestClientService restClientService;

    @Value("${vendor.api.base-url:http://test.vendor.api}")
    private String vendorApiBaseUrl;

    @Value("${vendor.api.timeout:5000}")
    private int vendorApiTimeout;

    public VendorService(RestClientService restClientService) {
        this.restClientService = restClientService;
    }

    /**
     * Checks if a vendor exists in the external system.
     * Uses HEAD request to check existence without fetching full details.
     *
     * @param entityId The entity ID (UUID string)
     * @param vendorKey The vendor key (integer) to check
     * @return true if vendor exists, false otherwise
     */
    public boolean vendorExists(String entityId, Integer vendorKey) {
        if (entityId == null || entityId.trim().isEmpty()) {
            logger.warn("Entity ID is null or empty, cannot check vendor existence");
            return false;
        }
        
        if (vendorKey == null) {
            logger.warn("Vendor key is null, cannot check existence");
            return false;
        }

        try {
            String url = buildVendorUrl(entityId, vendorKey);
            logger.debug("Checking if vendor exists: entityId={}, vendorKey={}, url={}", entityId, vendorKey, url);
            
            boolean exists = restClientService.exists(url);
            logger.debug("Vendor existence check result: entityId={}, vendorKey={}, exists={}", entityId, vendorKey, exists);
            return exists;
        } catch (RestClientException e) {
            logger.error("Error checking vendor existence for entityId={}, vendorKey={}: {}", entityId, vendorKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fetches vendor details from the external API.
     * Endpoint: GET /api/v1/entities/:entityId/vendors/:id
     *
     * @param entityId The entity ID (UUID string)
     * @param vendorKey The vendor key (integer) to fetch
     * @param include Optional comma-separated list of fields to include in response
     * @return Optional containing VendorDTO if found, empty otherwise
     */
    public Optional<VendorDTO> getVendorDetails(String entityId, Integer vendorKey, String include) {
        if (entityId == null || entityId.trim().isEmpty()) {
            logger.warn("Entity ID is null or empty, cannot fetch vendor details");
            return Optional.empty();
        }
        
        if (vendorKey == null) {
            logger.warn("Vendor key is null, cannot fetch vendor details");
            return Optional.empty();
        }

        try {
            String url = buildVendorUrl(entityId, vendorKey, include);
            logger.debug("Fetching vendor details: entityId={}, vendorKey={}, url={}", entityId, vendorKey, url);
            
            ResponseEntity<VendorDTO> response = restClientService.get(url, VendorDTO.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.debug("Successfully fetched vendor details: entityId={}, vendorKey={}", entityId, vendorKey);
                return Optional.of(response.getBody());
            } else {
                logger.warn("Vendor not found or empty response: entityId={}, vendorKey={}", entityId, vendorKey);
                return Optional.empty();
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Vendor not found: entityId={}, vendorKey={}", entityId, vendorKey);
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            logger.error("Client error fetching vendor details for entityId={}, vendorKey={}: {}", entityId, vendorKey, e.getMessage());
            return Optional.empty();
        } catch (RestClientException e) {
            logger.error("Error fetching vendor details for entityId={}, vendorKey={}: {}", entityId, vendorKey, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Fetches vendor details from the external API without include parameter.
     *
     * @param entityId The entity ID (UUID string)
     * @param vendorKey The vendor key (integer) to fetch
     * @return Optional containing VendorDTO if found, empty otherwise
     */
    public Optional<VendorDTO> getVendorDetails(String entityId, Integer vendorKey) {
        return getVendorDetails(entityId, vendorKey, null);
    }

    /**
     * Gets vendor address from external API.
     * This is a convenience method that fetches vendor details and extracts the primary address.
     *
     * @param entityId The entity ID (UUID string)
     * @param vendorKey The vendor key (integer)
     * @return Optional containing the vendor address if found, empty otherwise
     */
    public Optional<String> getVendorAddress(String entityId, Integer vendorKey) {
        Optional<VendorDTO> vendor = getVendorDetails(entityId, vendorKey);
        return vendor.map(VendorDTO::getPrimaryAddress);
    }

    /**
     * Builds the vendor API URL.
     * Format: {baseUrl}/api/v1/entities/{entityId}/vendors/{vendorKey}
     *
     * @param entityId The entity ID
     * @param vendorKey The vendor key
     * @return Complete URL string
     */
    private String buildVendorUrl(String entityId, Integer vendorKey) {
        return buildVendorUrl(entityId, vendorKey, null);
    }

    /**
     * Builds the vendor API URL with optional include parameter.
     * Format: {baseUrl}/api/v1/entities/{entityId}/vendors/{vendorKey}?include={include}
     *
     * @param entityId The entity ID
     * @param vendorKey The vendor key
     * @param include Optional comma-separated fields to include
     * @return Complete URL string
     */
    private String buildVendorUrl(String entityId, Integer vendorKey, String include) {
        String baseUrl = vendorApiBaseUrl.endsWith("/") ? 
            vendorApiBaseUrl.substring(0, vendorApiBaseUrl.length() - 1) : vendorApiBaseUrl;
        
        String url = String.format("%s/api/v1/entities/%s/vendors/%d", baseUrl, entityId, vendorKey);
        
        if (include != null && !include.trim().isEmpty()) {
            url += "?include=" + include.trim();
        }
        
        return url;
    }
}

