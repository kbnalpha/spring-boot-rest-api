package com.ehspro.repository;
import com.ehspro.entity.Equipment;
import org.springframework.data.jpa.repository.*;
public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {
}

