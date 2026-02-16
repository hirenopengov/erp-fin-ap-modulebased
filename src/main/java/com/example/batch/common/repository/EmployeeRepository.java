package com.opengov.erp.ap.common.repository;

import com.opengov.erp.ap.common.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    List<Employee> findByDepartment(String department);

    List<Employee> findByStatus(String status);

    @Query("SELECT e FROM Employee e WHERE e.salary >= :minSalary")
    List<Employee> findBySalaryGreaterThanEqual(@Param("minSalary") Double minSalary);

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :department")
    Double findAverageSalaryByDepartment(@Param("department") String department);

    boolean existsByEmployeeId(String employeeId);
}
