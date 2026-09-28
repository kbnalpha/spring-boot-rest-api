package com.ehspro.repository;
import com.ehspro.entity.OrganizationUnit;
import org.springframework.data.jpa.repository.*;
public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, Long>, JpaSpecificationExecutor<OrganizationUnit> {
}

