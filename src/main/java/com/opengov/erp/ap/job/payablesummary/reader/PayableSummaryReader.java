package com.opengov.erp.ap.job.payablesummary.reader;

import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class PayableSummaryReader {

    @Autowired
    private DataSource dataSource;

    @StepScope
    public JdbcCursorItemReader<PayableSummaryCSVDTO> reader(
            @Value("#{jobParameters['paymentRunId']}") String paymentRunId,
            @Value("#{jobParameters['entityId']}") String entityId) {

        // SQL query to aggregate payables by vendor
        // This query joins pr_ap_payable_run_item -> pr_ap_payable -> pr_ap_invoice_header
        // and aggregates by vendor
        String sql = "SELECT " +
                "    inv.ap_vendor_id as vendorId, " +
                "    CAST(inv.ap_vendor_id AS VARCHAR) as vendorNumber, " +
                "    COALESCE(SUM(COALESCE(pri.computed_amount_to_pay, pri.requested_amount_to_pay, 0)), 0) as totalPayableAmount, " +
                "    COUNT(DISTINCT inv.id) as invoiceCount " +
                "FROM pr_ap_payable_run_item pri " +
                "INNER JOIN pr_ap_payable pay ON pri.payable_id = pay.id " +
                "INNER JOIN pr_ap_invoice_header inv ON pay.invoice_id = inv.id " +
                "WHERE pri.payment_run_id = ? " +
                "  AND pri.entity_id = ? " +
                "GROUP BY inv.ap_vendor_id " +
                "ORDER BY inv.ap_vendor_id";

        return new JdbcCursorItemReaderBuilder<PayableSummaryCSVDTO>()
                .name("payableSummaryReader")
                .dataSource(dataSource)
                .sql(sql)
                .queryArguments(UUID.fromString(paymentRunId), UUID.fromString(entityId))
                .rowMapper(new RowMapper<PayableSummaryCSVDTO>() {
                    @Override
                    public PayableSummaryCSVDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                        PayableSummaryCSVDTO dto = new PayableSummaryCSVDTO();
                        dto.setVendorId(rs.getInt("vendorId"));
                        dto.setVendorNumber(rs.getString("vendorNumber"));
                        dto.setTotalPayableAmount(rs.getDouble("totalPayableAmount"));
                        dto.setInvoiceCount(rs.getInt("invoiceCount"));
                        // Reference number, status, and dateCreated will be set in processor
                        return dto;
                    }
                })
                .build();
    }
}

