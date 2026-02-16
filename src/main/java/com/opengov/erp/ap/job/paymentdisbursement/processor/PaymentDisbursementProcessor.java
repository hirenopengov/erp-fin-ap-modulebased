package com.opengov.erp.ap.job.paymentdisbursement.processor;

import com.opengov.erp.ap.common.dto.EmployeeCSVDTO;
import com.opengov.erp.ap.common.service.EmployeeService;
import com.opengov.erp.ap.common.mapper.EmployeeMapper;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PaymentDisbursementProcessor implements ItemProcessor<EmployeeCSVDTO, EmployeeCSVDTO> {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;
    private StepExecution stepExecution;
    private Double taxRate = 5.0; // Default tax rate

    public PaymentDisbursementProcessor(EmployeeService employeeService, EmployeeMapper employeeMapper) {
        this.employeeService = employeeService;
        this.employeeMapper = employeeMapper;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        // Get tax rate from job parameters, default to 5.0
        org.springframework.batch.core.JobParameters params = stepExecution.getJobParameters();
        try {
            Double doubleValue = params.getDouble("taxRate");
            if (doubleValue != null) {
                this.taxRate = doubleValue;
            }
        } catch (IllegalArgumentException e) {
            // Try as Long
            try {
                Long longValue = params.getLong("taxRate");
                if (longValue != null) {
                    this.taxRate = longValue.doubleValue();
                }
            } catch (IllegalArgumentException e2) {
                // Try as String
                try {
                    String taxRateParam = params.getString("taxRate");
                    if (taxRateParam != null) {
                        try {
                            this.taxRate = Double.parseDouble(taxRateParam);
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
        // Process the employee for disbursement - apply business logic
        EmployeeCSVDTO processedEmployee = new EmployeeCSVDTO();
        processedEmployee.setId(employeeCSV.getId());
        processedEmployee.setName(employeeCSV.getName());
        processedEmployee.setDepartment(employeeCSV.getDepartment());
        
        // Calculate disbursement amount (net salary after deductions)
        // Apply tax rate from parameter
        double taxDeduction = employeeCSV.getSalary() * (taxRate / 100.0);
        double netSalary = employeeCSV.getSalary() - taxDeduction;
        processedEmployee.setSalary(netSalary);
        
        // Optionally save to database
        // Employee entity = employeeMapper.toEntity(processedEmployee.getId(), 
        //     processedEmployee.getName(), processedEmployee.getDepartment(), 
        //     processedEmployee.getSalary());
        // employeeService.createOrUpdate(entity);
        
        return processedEmployee;
    }
}
