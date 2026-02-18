package com.opengov.erp.ap.job.payablesummary.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.dto.PayableSummaryCSVDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Writer that writes PayableSummary data to JSON file.
 * This writer accumulates all items and writes them as a JSON array.
 */
@Component
@StepScope
public class PayableSummaryJsonWriter implements ItemWriter<PayableSummaryCSVDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PayableSummaryJsonWriter.class);
    
    private final ObjectMapper objectMapper;
    private final String jsonFilePath;
    private final List<PayableSummaryCSVDTO> items;

    public PayableSummaryJsonWriter(@Value("#{jobParameters['paymentRunId']}") String paymentRunId) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.jsonFilePath = Paths.get(Constants.FilePaths.OUTPUT_DIR, "PayableSummary.json").toString();
        this.items = new ArrayList<>();
    }

    @Override
    public void write(Chunk<? extends PayableSummaryCSVDTO> chunk) throws Exception {
        logger.debug("Accumulating {} items for JSON file", chunk.size());
        this.items.addAll(chunk.getItems());
        
        // Write to file after each chunk
        writeToFile();
    }

    /**
     * Writes all accumulated items to JSON file.
     */
    private void writeToFile() throws Exception {
        File jsonFile = new File(jsonFilePath);
        
        // Ensure parent directory exists
        File parentDir = jsonFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        logger.debug("Writing {} items to JSON file: {}", items.size(), jsonFilePath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, items);
    }

    /**
     * Gets the JSON file path.
     *
     * @return JSON file path
     */
    public String getJsonFilePath() {
        return jsonFilePath;
    }

    /**
     * Gets the accumulated items as JSON string.
     *
     * @return JSON string representation of items
     * @throws Exception if JSON serialization fails
     */
    public String getJsonString() throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(items);
    }

    /**
     * Gets the accumulated items.
     *
     * @return List of PayableSummaryCSVDTO items
     */
    public List<PayableSummaryCSVDTO> getItems() {
        return items;
    }
}

