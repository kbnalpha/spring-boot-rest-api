package com.ehspro.repository;
import com.ehspro.entity.OperationActivity;
import org.springframework.data.jpa.repository.*;
public interface OperationActivityRepository extends JpaRepository<OperationActivity, Long>, JpaSpecificationExecutor<OperationActivity> {
}

