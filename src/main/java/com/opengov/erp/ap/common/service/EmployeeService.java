package com.opengov.erp.ap.common.service;

import com.opengov.erp.ap.common.model.Employee;
import com.opengov.erp.ap.common.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService extends BaseService<Employee, Long> {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected EmployeeRepository getRepository() {
        return employeeRepository;
    }

    public Optional<Employee> findByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    public List<Employee> findByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    public List<Employee> findByStatus(String status) {
        return employeeRepository.findByStatus(status);
    }

    public List<Employee> findBySalaryGreaterThanEqual(Double minSalary) {
        return employeeRepository.findBySalaryGreaterThanEqual(minSalary);
    }

    public Double getAverageSalaryByDepartment(String department) {
        return employeeRepository.findAverageSalaryByDepartment(department);
    }

    public boolean existsByEmployeeId(String employeeId) {
        return employeeRepository.existsByEmployeeId(employeeId);
    }

    public Employee createOrUpdate(Employee employee) {
        Optional<Employee> existing = employeeRepository.findByEmployeeId(employee.getEmployeeId());
        if (existing.isPresent()) {
            Employee existingEmployee = existing.get();
            existingEmployee.setName(employee.getName());
            existingEmployee.setDepartment(employee.getDepartment());
            existingEmployee.setSalary(employee.getSalary());
            existingEmployee.setStatus(employee.getStatus());
            return employeeRepository.save(existingEmployee);
        }
        return employeeRepository.save(employee);
    }

    public Employee applySalaryBonus(String employeeId, double bonusPercentage) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            double bonus = employee.getSalary() * (bonusPercentage / 100);
            employee.setSalary(employee.getSalary() + bonus);
            return employeeRepository.save(employee);
        }
        throw new RuntimeException("Employee not found with ID: " + employeeId);
    }
}
