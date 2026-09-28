package com.ehspro.repository;
import com.ehspro.entity.Designation;
import org.springframework.data.jpa.repository.*;
public interface DesignationRepository extends JpaRepository<Designation, Long>, JpaSpecificationExecutor<Designation> {
}

