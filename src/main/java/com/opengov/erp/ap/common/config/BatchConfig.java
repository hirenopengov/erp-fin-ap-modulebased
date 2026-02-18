package com.opengov.erp.ap.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Explicitly initialize Spring Batch metadata tables.
     * This ensures tables are created even if automatic initialization fails.
     * Uses Spring Batch's built-in PostgreSQL schema script.
     */
    @Bean
    public DataSourceInitializer batchDataSourceInitializer() {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(batchDatabasePopulator());
        initializer.setEnabled(true);
        
        logger.info("Spring Batch schema initialization configured");
        return initializer;
    }

    private DatabasePopulator batchDatabasePopulator() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        // Spring Batch provides schema scripts in the classpath
        // For PostgreSQL, use the postgresql-specific script
        // Using classpath: prefix to ensure it's found in JAR files
        populator.addScript(resourceLoader.getResource("classpath:org/springframework/batch/core/schema-postgresql.sql"));
        // Continue on error so it doesn't fail if tables already exist
        populator.setContinueOnError(true);
        populator.setIgnoreFailedDrops(true);
        
        logger.debug("Spring Batch PostgreSQL schema script will be executed");
        return populator;
    }

    /**
     * RestTemplate bean for making REST API calls.
     * Can be used throughout the application for external API interactions.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
