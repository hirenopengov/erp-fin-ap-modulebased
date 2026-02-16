package com.opengov.erp.ap.common.constants;

public class Constants {

    private Constants() {
        // Utility class
    }

    public static class EmployeeStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
        public static final String TERMINATED = "TERMINATED";
    }

    public static class BatchJob {
        public static final String PAYMENT_PROCESSING = "paymentProcessingJob";
        public static final String PAYMENT_DISBURSEMENT = "paymentDisbursementJob";
        public static final int DEFAULT_CHUNK_SIZE = 10;
    }

    public static class FilePaths {
        public static final String INPUT_DIR = "data/input/";
        public static final String OUTPUT_DIR = "output/";
        public static final String EMPLOYEES_CSV = "employees.csv";
        public static final String PROCESSED_EMPLOYEES_CSV = "processed_employees.csv";
        public static final String DISBURSED_EMPLOYEES_CSV = "disbursed_employees.csv";
    }
}
