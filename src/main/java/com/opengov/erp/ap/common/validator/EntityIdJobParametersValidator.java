package com.opengov.erp.ap.common.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

/**
 * Validator to ensure entityId parameter is present and not empty.
 * This validator makes entityId mandatory for all batch jobs to ensure
 * proper multitenancy support.
 */
public class EntityIdJobParametersValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        if (parameters == null) {
            throw new JobParametersInvalidException("Job parameters cannot be null. entityId parameter is required.");
        }

        String entityId = parameters.getString("entityId");
        
        if (!StringUtils.hasText(entityId)) {
            throw new JobParametersInvalidException(
                "Missing required parameter: entityId. " +
                "Please provide entityId parameter when running the job. " +
                "Example: java -jar app.jar run <jobName> entityId=TENANT001"
            );
        }
    }
}

