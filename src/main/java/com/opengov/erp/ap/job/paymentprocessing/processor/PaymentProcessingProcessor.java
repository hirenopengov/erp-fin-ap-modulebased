package com.opengov.erp.ap.job.paymentprocessing.processor;

import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import com.opengov.erp.ap.common.service.EmployeeService;
import com.opengov.erp.ap.common.mapper.EmployeeMapper;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessingProcessor implements ItemProcessor<EmployeeCSVDTO, EmployeeCSVDTO> {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;
    private StepExecution stepExecution;
    private Double bonusPercentage = 10.0; // Default bonus percentage

    public PaymentProcessingProcessor(EmployeeService employeeService, EmployeeMapper employeeMapper) {
        this.employeeService = employeeService;
        this.employeeMapper = employeeMapper;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        // Get bonus percentage from job parameters, default to 10.0
        org.springframework.batch.core.JobParameters params = stepExecution.getJobParameters();
        try {
            Double doubleValue = params.getDouble("bonusPercentage");
            if (doubleValue != null) {
                this.bonusPercentage = doubleValue;
            }
        } catch (IllegalArgumentException e) {
            // Try as Long
            try {
                Long longValue = params.getLong("bonusPercentage");
                if (longValue != null) {
                    this.bonusPercentage = longValue.doubleValue();
                }
            } catch (IllegalArgumentException e2) {
                // Try as String
                try {
                    String bonusParam = params.getString("bonusPercentage");
                    if (bonusParam != null) {
                        try {
                            this.bonusPercentage = Double.parseDouble(bonusParam);
                        } catch (NumberFormatException e3) {
                            // Use default if number format is invalid
                        }
                    }
                } catch (IllegalArgumentException e4) {
                    // Use default if parameter doesn't exist or wrong type
                }
            }
        }
    }

    @Override
    public EmployeeCSVDTO process(EmployeeCSVDTO employeeCSV) throws Exception {
        // Process the employee - apply business logic
        EmployeeCSVDTO processedEmployee = new EmployeeCSVDTO();
        processedEmployee.setId(employeeCSV.getId());
        processedEmployee.setName(employeeCSV.getName().toUpperCase()); // Convert name to uppercase
        processedEmployee.setDepartment(employeeCSV.getDepartment());
        
        // Apply bonus percentage from parameter
        double bonus = employeeCSV.getSalary() * (bonusPercentage / 100.0);
        processedEmployee.setSalary(employeeCSV.getSalary() + bonus);
        
        // Optionally save to database
        // Employee entity = employeeMapper.toEntity(processedEmployee.getId(), 
        //     processedEmployee.getName(), processedEmployee.getDepartment(), 
        //     processedEmployee.getSalary());
        // employeeService.createOrUpdate(entity);
        
        return processedEmployee;
    }
}
