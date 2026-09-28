package com.ehspro.repository;
import com.ehspro.entity.Location;
import org.springframework.data.jpa.repository.*;
public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
}

