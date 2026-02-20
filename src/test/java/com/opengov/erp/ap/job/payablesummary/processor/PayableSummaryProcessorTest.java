package com.opengov.erp.ap.job.payablesummary.processor;

import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import com.opengov.erp.ap.common.service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayableSummaryProcessorTest {

    @Mock
    private VendorService vendorService;

    @InjectMocks
    private PayableSummaryProcessor processor;

    private static final String ENTITY_ID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        // Set entityId using reflection since it's injected via @Value
        ReflectionTestUtils.setField(processor, "entityId", ENTITY_ID);
    }

    @Test
    void testProcess_WithValidVendor_ShouldEnrichWithAddress() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(123);
        input.setVendorNumber("V123");
        input.setTotalPayableAmount(1000.0);
        input.setInvoiceCount(5);

        String expectedAddress = "123 Main St, City, State 12345";
        when(vendorService.vendorExists(ENTITY_ID, 123)).thenReturn(true);
        when(vendorService.getVendorAddress(ENTITY_ID, 123)).thenReturn(Optional.of(expectedAddress));

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result);
        assertEquals(input.getVendorId(), result.getVendorId());
        assertEquals(input.getVendorNumber(), result.getVendorNumber());
        assertEquals(input.getTotalPayableAmount(), result.getTotalPayableAmount());
        assertEquals(input.getInvoiceCount(), result.getInvoiceCount());
        assertNotNull(result.getReferenceNumber());
        assertTrue(result.getReferenceNumber().startsWith("VENDOR-123-"));
        assertEquals("DRAFT", result.getStatus());
        assertNotNull(result.getDateCreated());
        assertEquals(expectedAddress, result.getAddress());

        verify(vendorService).vendorExists(ENTITY_ID, 123);
        verify(vendorService).getVendorAddress(ENTITY_ID, 123);
    }

    @Test
    void testProcess_WithVendorNotExists_ShouldSetEmptyAddress() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(456);
        input.setVendorNumber("V456");
        input.setTotalPayableAmount(2000.0);
        input.setInvoiceCount(10);

        when(vendorService.vendorExists(ENTITY_ID, 456)).thenReturn(false);

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getAddress());
        assertEquals("DRAFT", result.getStatus());
        assertNotNull(result.getReferenceNumber());

        verify(vendorService).vendorExists(ENTITY_ID, 456);
        verify(vendorService, never()).getVendorAddress(anyString(), any());
    }

    @Test
    void testProcess_WithVendorExistsButNoAddress_ShouldSetEmptyAddress() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(789);
        input.setVendorNumber("V789");
        input.setTotalPayableAmount(3000.0);
        input.setInvoiceCount(15);

        when(vendorService.vendorExists(ENTITY_ID, 789)).thenReturn(true);
        when(vendorService.getVendorAddress(ENTITY_ID, 789)).thenReturn(Optional.empty());

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getAddress());
        verify(vendorService).vendorExists(ENTITY_ID, 789);
        verify(vendorService).getVendorAddress(ENTITY_ID, 789);
    }

    @Test
    void testProcess_WithNullVendorId_ShouldSetEmptyAddress() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(null);
        input.setVendorNumber("V999");
        input.setTotalPayableAmount(4000.0);
        input.setInvoiceCount(20);

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getAddress());
        assertEquals("DRAFT", result.getStatus());
        assertNotNull(result.getReferenceNumber());

        verify(vendorService, never()).vendorExists(anyString(), any());
        verify(vendorService, never()).getVendorAddress(anyString(), any());
    }

    @Test
    void testProcess_WithNullEntityId_ShouldSetEmptyAddress() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(processor, "entityId", null);
        
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(111);
        input.setVendorNumber("V111");
        input.setTotalPayableAmount(5000.0);
        input.setInvoiceCount(25);

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getAddress());
        verify(vendorService, never()).vendorExists(anyString(), any());
        verify(vendorService, never()).getVendorAddress(anyString(), any());
    }

    @Test
    void testProcess_WithEmptyEntityId_ShouldSetEmptyAddress() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(processor, "entityId", "");
        
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(222);
        input.setVendorNumber("V222");
        input.setTotalPayableAmount(6000.0);
        input.setInvoiceCount(30);

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getAddress());
        verify(vendorService, never()).vendorExists(anyString(), any());
        verify(vendorService, never()).getVendorAddress(anyString(), any());
    }

    @Test
    void testProcess_ReferenceNumberFormat_ShouldBeCorrect() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(333);
        input.setVendorNumber("V333");
        input.setTotalPayableAmount(7000.0);
        input.setInvoiceCount(35);

        when(vendorService.vendorExists(ENTITY_ID, 333)).thenReturn(false);

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result.getReferenceNumber());
        assertTrue(result.getReferenceNumber().matches("VENDOR-333-[A-Z0-9]{8}"));
    }

    @Test
    void testProcess_DateCreated_ShouldBeSet() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(444);
        input.setVendorNumber("V444");
        input.setTotalPayableAmount(8000.0);
        input.setInvoiceCount(40);

        when(vendorService.vendorExists(ENTITY_ID, 444)).thenReturn(false);

        Instant beforeProcessing = Instant.now();

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertNotNull(result.getDateCreated());
        assertTrue(result.getDateCreated().isAfter(beforeProcessing.minusSeconds(1)));
        assertTrue(result.getDateCreated().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testProcess_Status_ShouldAlwaysBeDraft() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(555);
        input.setVendorNumber("V555");
        input.setTotalPayableAmount(9000.0);
        input.setInvoiceCount(45);

        when(vendorService.vendorExists(ENTITY_ID, 555)).thenReturn(false);

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertEquals("DRAFT", result.getStatus());
    }

    @Test
    void testProcess_WithEmptyAddressString_ShouldSetEmptyAddress() throws Exception {
        // Arrange
        PayableSummaryCSVDTO input = new PayableSummaryCSVDTO();
        input.setVendorId(666);
        input.setVendorNumber("V666");
        input.setTotalPayableAmount(10000.0);
        input.setInvoiceCount(50);

        when(vendorService.vendorExists(ENTITY_ID, 666)).thenReturn(true);
        when(vendorService.getVendorAddress(ENTITY_ID, 666)).thenReturn(Optional.of("   "));

        // Act
        PayableSummaryCSVDTO result = processor.process(input);

        // Assert
        assertEquals("", result.getAddress());
    }
}

