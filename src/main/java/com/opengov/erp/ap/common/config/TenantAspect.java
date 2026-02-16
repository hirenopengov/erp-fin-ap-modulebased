package com.opengov.erp.ap.common.config;

import com.opengov.erp.ap.common.context.TenantContext;
import com.opengov.erp.ap.common.model.BaseEntity;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {

    private static final Logger logger = LoggerFactory.getLogger(TenantAspect.class);

    @Before("execution(* com.opengov.erp.ap.common.repository.*.save*(..))")
    public void setTenantBeforeSave(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof BaseEntity entity) {
                if (entity.getEntityId() == null && TenantContext.hasTenant()) {
                    entity.setEntityId(TenantContext.getCurrentTenant());
                    logger.debug("Set entity_id {} on entity before save", TenantContext.getCurrentTenant());
                }
            } else if (arg instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (item instanceof BaseEntity entity) {
                        if (entity.getEntityId() == null && TenantContext.hasTenant()) {
                            entity.setEntityId(TenantContext.getCurrentTenant());
                            logger.debug("Set entity_id {} on entity in collection before save", TenantContext.getCurrentTenant());
                        }
                    }
                }
            }
        }
    }
}
