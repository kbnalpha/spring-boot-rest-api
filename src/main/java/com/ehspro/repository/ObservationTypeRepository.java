package com.ehspro.repository;
import com.ehspro.entity.ObservationType;
import org.springframework.data.jpa.repository.*;
public interface ObservationTypeRepository extends JpaRepository<ObservationType, Long>, JpaSpecificationExecutor<ObservationType> {
}

