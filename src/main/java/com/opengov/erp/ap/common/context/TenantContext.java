package com.opengov.erp.ap.common.context;

public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            currentTenant.set(tenantId);
        } else {
            currentTenant.remove();
        }
    }

    public static String getCurrentTenant() {
        String tenant = currentTenant.get();
        if (tenant == null) {
            throw new IllegalStateException("No tenant context set. Please set entity_id before performing database operations.");
        }
        return tenant;
    }

    public static String getCurrentTenantOrNull() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }

    public static boolean hasTenant() {
        return currentTenant.get() != null;
    }
}
