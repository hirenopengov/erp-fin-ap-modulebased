package com.opengov.erp.ap.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCSVDTO {
    private String id;
    private String name;
    private String department;
    private Double salary;
}
