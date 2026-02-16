package com.opengov.erp.ap.common.runner;

import com.opengov.erp.ap.common.constants.Constants;
import com.opengov.erp.ap.common.service.JobLauncherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class JobCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(JobCommandLineRunner.class);

    private final JobRegistry jobRegistry;
    private final JobLauncherService jobLauncherService;

    public JobCommandLineRunner(JobRegistry jobRegistry, JobLauncherService jobLauncherService) {
        this.jobRegistry = jobRegistry;
        this.jobLauncherService = jobLauncherService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "run":
                if (args.length < 2) {
                    logger.error("Job name is required. Usage: run <jobName> [param1=value1] [param2=value2] ...");
                    printUsage();
                    return;
                }
                runJob(args);
                break;
            case "list":
                listJobs();
                break;
            case "help":
            default:
                printUsage();
                break;
        }
    }

    private void runJob(String[] args) {
        try {
            String jobName = args[1];
            Job job = jobRegistry.getJob(jobName);

            // Extract parameters (everything after job name)
            String[] params = Arrays.copyOfRange(args, 2, args.length);
            JobParameters jobParameters = jobLauncherService.createJobParameters(params);

            logger.info("Executing job: {} with parameters: {}", jobName, jobParameters);
            jobLauncherService.runJob(job, jobParameters);

        } catch (NoSuchJobException e) {
            logger.error("Job not found: {}", args[1]);
            logger.info("Available jobs:");
            listJobs();
        } catch (Exception e) {
            logger.error("Error executing job", e);
        }
    }

    private void listJobs() {
        logger.info("Available jobs:");
        logger.info("  - {}", Constants.BatchJob.PAYMENT_PROCESSING);
        logger.info("  - {}", Constants.BatchJob.PAYMENT_DISBURSEMENT);
    }

    private void printUsage() {
        logger.info("=== Spring Batch Job Runner ===");
        logger.info("");
        logger.info("Usage:");
        logger.info("  java -jar app.jar run <jobName> [param1=value1] [param2=value2] ...");
        logger.info("");
        logger.info("Examples:");
        logger.info("  java -jar app.jar run paymentProcessingJob bonusPercentage=15.0 inputFile=employees.csv");
        logger.info("  java -jar app.jar run paymentDisbursementJob taxRate=7.5 outputFile=disbursed.csv");
        logger.info("");
        logger.info("Commands:");
        logger.info("  run <jobName> [params]  - Run a specific job with optional parameters");
        logger.info("  list                    - List all available jobs");
        logger.info("  help                    - Show this help message");
        logger.info("");
        logger.info("Available Jobs:");
        logger.info("  - paymentProcessingJob");
        logger.info("  - paymentDisbursementJob");
    }
}
