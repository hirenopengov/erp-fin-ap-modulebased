package com.opengov.erp.ap.job.payablesummary.writer;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class PayableSummaryWriter {

    @StepScope
    public FlatFileItemWriter<PayableSummaryCSVDTO> writer() {
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
                        item.getDateCreated().toString() : ""
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
                .headerCallback(writer -> writer.write("vendorId,vendorNumber,totalPayableAmount,invoiceCount,referenceNumber,status,dateCreated"))
                .build();
    }
}

