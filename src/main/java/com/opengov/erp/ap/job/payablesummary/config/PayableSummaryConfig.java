package com.opengov.erp.ap.job.payablesummary.config;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import com.opengov.erp.ap.common.service.VendorService;
import com.opengov.erp.ap.common.validator.EntityIdJobParametersValidator;
import com.opengov.erp.ap.job.payablesummary.listener.PayableSummaryItemProcessListener;
import com.opengov.erp.ap.job.payablesummary.listener.PayableSummaryItemReadListener;
import com.opengov.erp.ap.job.payablesummary.listener.PayableSummaryItemWriteListener;
import com.opengov.erp.ap.job.payablesummary.listener.PayableSummaryStepListener;
import com.opengov.erp.ap.job.payablesummary.processor.PayableSummaryProcessor;
import com.opengov.erp.ap.job.payablesummary.writer.PayableSummaryJsonWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Configuration
public class PayableSummaryConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final VendorService vendorService;
    private final PayableSummaryStepListener payableSummaryStepListener;
    private final PayableSummaryItemReadListener payableSummaryItemReadListener;
    private final PayableSummaryItemProcessListener payableSummaryItemProcessListener;
    private final PayableSummaryItemWriteListener payableSummaryItemWriteListener;
    
    @Autowired
    private DataSource dataSource;

    public PayableSummaryConfig(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               VendorService vendorService,
                               PayableSummaryStepListener payableSummaryStepListener,
                               PayableSummaryItemReadListener payableSummaryItemReadListener,
                               PayableSummaryItemProcessListener payableSummaryItemProcessListener,
                               PayableSummaryItemWriteListener payableSummaryItemWriteListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.vendorService = vendorService;
        this.payableSummaryStepListener = payableSummaryStepListener;
        this.payableSummaryItemReadListener = payableSummaryItemReadListener;
        this.payableSummaryItemProcessListener = payableSummaryItemProcessListener;
        this.payableSummaryItemWriteListener = payableSummaryItemWriteListener;
    }

    @Bean
    public Job payableSummaryJob(Step payableSummaryStep) {
        return new JobBuilder("payableSummaryJob", jobRepository)
                .validator(new EntityIdJobParametersValidator())
                .start(payableSummaryStep)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<PayableSummaryCSVDTO> payableSummaryReaderBean(
            @Value("#{jobParameters['paymentRunId']}") String paymentRunId,
            @Value("#{jobParameters['entityId']}") String entityId) {
        
        // SQL query to aggregate payables by vendor
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
                        return dto;
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<PayableSummaryCSVDTO> payableSummaryWriterBean() {
        // Always use PayableSummary.csv in the output directory
        String outputPath = Paths.get(Constants.FilePaths.OUTPUT_DIR, "PayableSummary.csv").toString();
        
        // Custom field extractor to format dateCreated as ISO-8601 string
        BeanWrapperFieldExtractor<PayableSummaryCSVDTO> fieldExtractor = new BeanWrapperFieldExtractor<PayableSummaryCSVDTO>() {
            @Override
            public Object[] extract(PayableSummaryCSVDTO item) {
                return new Object[]{
                    item.getVendorId(),
                    item.getVendorNumber(),
                    item.getTotalPayableAmount(),
                    item.getInvoiceCount(),
                    item.getReferenceNumber(),
                    item.getStatus(),
                    item.getDateCreated() != null ? 
                        item.getDateCreated().toString() : "",
                    item.getAddress() != null ? item.getAddress() : ""
                };
            }
        };
        
        DelimitedLineAggregator<PayableSummaryCSVDTO> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);
        
        return new FlatFileItemWriterBuilder<PayableSummaryCSVDTO>()
                .name("payableSummaryWriter")
                .resource(new FileSystemResource(outputPath))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("vendorId,vendorNumber,totalPayableAmount,invoiceCount,referenceNumber,status,dateCreated,address"))
                .build();
    }

    @Bean
    @StepScope
    public PayableSummaryProcessor payableSummaryProcessorBean() {
        return new PayableSummaryProcessor(vendorService);
    }

    @Bean
    @StepScope
    public PayableSummaryJsonWriter payableSummaryJsonWriterBean(
            @Value("#{jobParameters['paymentRunId']}") String paymentRunId) {
        return new PayableSummaryJsonWriter(paymentRunId);
    }

    @Bean
    @StepScope
    public CompositeItemWriter<PayableSummaryCSVDTO> compositePayableSummaryWriter(
            FlatFileItemWriter<PayableSummaryCSVDTO> payableSummaryWriterBean,
            PayableSummaryJsonWriter payableSummaryJsonWriterBean) {
        return new CompositeItemWriterBuilder<PayableSummaryCSVDTO>()
                .delegates(payableSummaryWriterBean, payableSummaryJsonWriterBean)
                .build();
    }

    @Bean
    public Step payableSummaryStep(
            JdbcCursorItemReader<PayableSummaryCSVDTO> payableSummaryReaderBean,
            PayableSummaryProcessor payableSummaryProcessorBean,
            CompositeItemWriter<PayableSummaryCSVDTO> compositePayableSummaryWriter,
            PayableSummaryJsonWriter payableSummaryJsonWriterBean) {
        return new StepBuilder("payableSummaryStep", jobRepository)
                .<PayableSummaryCSVDTO, PayableSummaryCSVDTO>chunk(Constants.BatchJob.DEFAULT_CHUNK_SIZE, transactionManager)
                .reader(payableSummaryReaderBean)
                .listener(payableSummaryItemReadListener)
                .processor(payableSummaryProcessorBean)
                .listener(payableSummaryItemProcessListener)
                .writer(compositePayableSummaryWriter)
                .listener(payableSummaryItemWriteListener)
                .listener(payableSummaryStepListener)
                .build();
    }
}

