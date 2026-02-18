package com.opengov.erp.ap.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String employeeId;
    private String name;
    private String department;
    private Double salary;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
