package com.opengov.erp.ap.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.opengov.erp.ap.common.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration
}
