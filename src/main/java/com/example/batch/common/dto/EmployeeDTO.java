package com.opengov.erp.ap.common.dto;

import java.time.LocalDateTime;

public class EmployeeDTO {
    private Long id;
    private String employeeId;
    private String name;
    private String department;
    private Double salary;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeDTO() {
    }

    public EmployeeDTO(Long id, String employeeId, String name, String department, Double salary, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
