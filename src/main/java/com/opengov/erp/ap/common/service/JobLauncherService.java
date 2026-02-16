package com.opengov.erp.ap.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JobLauncherService {

    private static final Logger logger = LoggerFactory.getLogger(JobLauncherService.class);

    private final JobLauncher jobLauncher;

    public JobLauncherService(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    public void runJob(Job job, JobParameters jobParameters) {
        try {
            logger.info("Starting job: {} with parameters: {}", job.getName(), jobParameters);
            jobLauncher.run(job, jobParameters);
            logger.info("Job {} completed successfully", job.getName());
        } catch (Exception e) {
            logger.error("Error running job: {}", job.getName(), e);
            throw new RuntimeException("Failed to run job: " + job.getName(), e);
        }
    }

    public JobParameters createJobParameters(String... params) {
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addDate("timestamp", new Date());
        
        // Parse key=value pairs
        for (String param : params) {
            if (param.contains("=")) {
                String[] keyValue = param.split("=", 2);
                String key = keyValue[0];
                String value = keyValue[1];
                
                // Try to parse as number
                try {
                    if (value.contains(".")) {
                        builder.addDouble(key, Double.parseDouble(value));
                    } else {
                        builder.addLong(key, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    // If not a number, add as string
                    builder.addString(key, value);
                }
            }
        }
        
        return builder.toJobParameters();
    }
}
