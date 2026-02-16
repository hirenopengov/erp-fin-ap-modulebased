package com.opengov.erp.ap.common.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    @Column(name = "employee_id", unique = true, nullable = false, length = 50)
    private String employeeId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "salary", precision = 10, scale = 2)
    private Double salary;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    public Employee() {
    }

    public Employee(String employeeId, String name, String department, Double salary) {
        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.salary = salary;
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

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + getId() +
                ", employeeId='" + employeeId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                ", status='" + status + '\'' +
                '}';
    }
}
