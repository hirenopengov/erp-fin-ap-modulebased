package com.opengov.erp.ap.common.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private ValidationUtil() {
        // Utility class
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("^E\\d{3,}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidEmployeeId(String employeeId) {
        return employeeId != null && EMPLOYEE_ID_PATTERN.matcher(employeeId).matches();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isPositive(Double value) {
        return value != null && value > 0;
    }

    public static void validateEmployeeId(String employeeId) {
        if (!isValidEmployeeId(employeeId)) {
            throw new IllegalArgumentException("Invalid employee ID format: " + employeeId);
        }
    }
}
