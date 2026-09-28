package com.ehspro.repository;
import com.ehspro.entity.Role;
import org.springframework.data.jpa.repository.*;
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
}

