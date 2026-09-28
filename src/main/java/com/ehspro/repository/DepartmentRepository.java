package com.ehspro.repository;
import com.ehspro.entity.Department;
import org.springframework.data.jpa.repository.*;
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
}

