package com.opengov.erp.ap.common.repository;

import com.opengov.erp.ap.common.context.TenantContext;
import com.opengov.erp.ap.common.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends BaseRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE e.employeeId = :employeeId AND e.entityId = :entityId")
    Optional<Employee> findByEmployeeIdAndEntityId(@Param("employeeId") String employeeId, @Param("entityId") String entityId);

    default Optional<Employee> findByEmployeeId(String employeeId) {
        return findByEmployeeIdAndEntityId(employeeId, TenantContext.getCurrentTenant());
    }

    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.entityId = :entityId")
    List<Employee> findByDepartmentAndEntityId(@Param("department") String department, @Param("entityId") String entityId);

    default List<Employee> findByDepartment(String department) {
        return findByDepartmentAndEntityId(department, TenantContext.getCurrentTenant());
    }

    @Query("SELECT e FROM Employee e WHERE e.status = :status AND e.entityId = :entityId")
    List<Employee> findByStatusAndEntityId(@Param("status") String status, @Param("entityId") String entityId);

    default List<Employee> findByStatus(String status) {
        return findByStatusAndEntityId(status, TenantContext.getCurrentTenant());
    }

    @Query("SELECT e FROM Employee e WHERE e.salary >= :minSalary AND e.entityId = :entityId")
    List<Employee> findBySalaryGreaterThanEqualAndEntityId(@Param("minSalary") Double minSalary, @Param("entityId") String entityId);

    default List<Employee> findBySalaryGreaterThanEqual(Double minSalary) {
        return findBySalaryGreaterThanEqualAndEntityId(minSalary, TenantContext.getCurrentTenant());
    }

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :department AND e.entityId = :entityId")
    Double findAverageSalaryByDepartmentAndEntityId(@Param("department") String department, @Param("entityId") String entityId);

    default Double findAverageSalaryByDepartment(String department) {
        return findAverageSalaryByDepartmentAndEntityId(department, TenantContext.getCurrentTenant());
    }

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE e.employeeId = :employeeId AND e.entityId = :entityId")
    boolean existsByEmployeeIdAndEntityId(@Param("employeeId") String employeeId, @Param("entityId") String entityId);

    default boolean existsByEmployeeId(String employeeId) {
        return existsByEmployeeIdAndEntityId(employeeId, TenantContext.getCurrentTenant());
    }
}
