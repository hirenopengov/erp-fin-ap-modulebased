package com.opengov.erp.ap.common.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Reusable REST API client service for making external API calls.
 * This service can be used throughout the application for any REST API interactions.
 */
@Service
public class RestClientService {

    private static final Logger logger = LoggerFactory.getLogger(RestClientService.class);
    
    private final RestTemplate restTemplate;

    public RestClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Performs a GET request to the specified URL.
     *
     * @param url The complete URL to call
     * @param responseType The expected response type
     * @param <T> The type of the response
     * @return ResponseEntity containing the response
     * @throws RestClientException if the request fails
     */
    public <T> ResponseEntity<T> get(String url, Class<T> responseType) {
        return get(url, responseType, null);
    }

    /**
     * Performs a GET request to the specified URL with custom headers.
     *
     * @param url The complete URL to call
     * @param responseType The expected response type
     * @param headers Custom headers to include in the request
     * @param <T> The type of the response
     * @return ResponseEntity containing the response
     * @throws RestClientException if the request fails
     */
    public <T> ResponseEntity<T> get(String url, Class<T> responseType, HttpHeaders headers) {
        try {
            logger.debug("Making GET request to: {}", url);
            HttpEntity<?> entity = headers != null ? new HttpEntity<>(headers) : null;
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            logger.debug("GET request successful. Status: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            logger.error("Client error calling GET {}: {}", url, e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            logger.error("Server error calling GET {}: {}", url, e.getMessage());
            throw e;
        } catch (RestClientException e) {
            logger.error("Error calling GET {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Performs a POST request to the specified URL.
     *
     * @param url The complete URL to call
     * @param requestBody The request body object
     * @param responseType The expected response type
     * @param <T> The type of the response
     * @param <R> The type of the request body
     * @return ResponseEntity containing the response
     * @throws RestClientException if the request fails
     */
    public <T, R> ResponseEntity<T> post(String url, R requestBody, Class<T> responseType) {
        return post(url, requestBody, responseType, null);
    }

    /**
     * Performs a POST request to the specified URL with custom headers.
     *
     * @param url The complete URL to call
     * @param requestBody The request body object
     * @param responseType The expected response type
     * @param headers Custom headers to include in the request
     * @param <T> The type of the response
     * @param <R> The type of the request body
     * @return ResponseEntity containing the response
     * @throws RestClientException if the request fails
     */
    public <T, R> ResponseEntity<T> post(String url, R requestBody, Class<T> responseType, HttpHeaders headers) {
        try {
            logger.debug("Making POST request to: {}", url);
            HttpHeaders requestHeaders = headers != null ? headers : createDefaultHeaders();
            HttpEntity<R> entity = new HttpEntity<>(requestBody, requestHeaders);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            logger.debug("POST request successful. Status: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            logger.error("Client error calling POST {}: {}", url, e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            logger.error("Server error calling POST {}: {}", url, e.getMessage());
            throw e;
        } catch (RestClientException e) {
            logger.error("Error calling POST {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Performs a PUT request to the specified URL.
     *
     * @param url The complete URL to call
     * @param requestBody The request body object
     * @param responseType The expected response type
     * @param <T> The type of the response
     * @param <R> The type of the request body
     * @return ResponseEntity containing the response
     * @throws RestClientException if the request fails
     */
    public <T, R> ResponseEntity<T> put(String url, R requestBody, Class<T> responseType) {
        return put(url, requestBody, responseType, null);
    }

    /**
     * Performs a PUT request to the specified URL with custom headers.
     *
     * @param url The complete URL to call
     * @param requestBody The request body object
     * @param responseType The expected response type
     * @param headers Custom headers to include in the request
     * @param <T> The type of the response
     * @param <R> The type of the request body
     * @return ResponseEntity containing the response
     * @throws RestClientException if the request fails
     */
    public <T, R> ResponseEntity<T> put(String url, R requestBody, Class<T> responseType, HttpHeaders headers) {
        try {
            logger.debug("Making PUT request to: {}", url);
            HttpHeaders requestHeaders = headers != null ? headers : createDefaultHeaders();
            HttpEntity<R> entity = new HttpEntity<>(requestBody, requestHeaders);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
            logger.debug("PUT request successful. Status: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            logger.error("Client error calling PUT {}: {}", url, e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            logger.error("Server error calling PUT {}: {}", url, e.getMessage());
            throw e;
        } catch (RestClientException e) {
            logger.error("Error calling PUT {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Performs a DELETE request to the specified URL.
     *
     * @param url The complete URL to call
     * @throws RestClientException if the request fails
     */
    public void delete(String url) {
        delete(url, null);
    }

    /**
     * Performs a DELETE request to the specified URL with custom headers.
     *
     * @param url The complete URL to call
     * @param headers Custom headers to include in the request
     * @throws RestClientException if the request fails
     */
    public void delete(String url, HttpHeaders headers) {
        try {
            logger.debug("Making DELETE request to: {}", url);
            HttpEntity<?> entity = headers != null ? new HttpEntity<>(headers) : null;
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            logger.debug("DELETE request successful");
        } catch (HttpClientErrorException e) {
            logger.error("Client error calling DELETE {}: {}", url, e.getMessage());
            throw e;
        } catch (HttpServerErrorException e) {
            logger.error("Server error calling DELETE {}: {}", url, e.getMessage());
            throw e;
        } catch (RestClientException e) {
            logger.error("Error calling DELETE {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Checks if a resource exists at the given URL (returns 200-299 status).
     *
     * @param url The URL to check
     * @return true if resource exists, false otherwise
     */
    public boolean exists(String url) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.HEAD, null, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            logger.warn("Error checking existence at {}: {}", url, e.getMessage());
            return false;
        } catch (RestClientException e) {
            logger.warn("Error checking existence at {}: {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Creates default HTTP headers with JSON content type.
     *
     * @return HttpHeaders with default settings
     */
    private HttpHeaders createDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Creates HTTP headers with authentication token.
     *
     * @param token The authentication token
     * @return HttpHeaders with authorization header
     */
    public HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = createDefaultHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    /**
     * Creates HTTP headers with custom headers map.
     *
     * @param customHeaders Map of header names to values
     * @return HttpHeaders with custom headers
     */
    public HttpHeaders createCustomHeaders(Map<String, String> customHeaders) {
        HttpHeaders headers = createDefaultHeaders();
        if (customHeaders != null) {
            customHeaders.forEach(headers::set);
        }
        return headers;
    }
}

