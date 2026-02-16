package com.opengov.erp.ap.common.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    // @EnableBatchProcessing automatically provides JobRegistry and JobRegistryBeanPostProcessor
    // No need to define them manually unless you need custom configuration
}
