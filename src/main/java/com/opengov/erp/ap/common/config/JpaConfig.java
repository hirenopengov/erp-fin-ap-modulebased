package com.opengov.erp.ap.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.opengov.erp.ap.common.repository")
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class JpaConfig {
    // JPA configuration with AOP support for tenant aspect
}
