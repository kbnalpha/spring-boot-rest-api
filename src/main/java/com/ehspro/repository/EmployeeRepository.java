package com.ehspro.repository;
import com.ehspro.entity.Employee;
import org.springframework.data.jpa.repository.*;
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
}

