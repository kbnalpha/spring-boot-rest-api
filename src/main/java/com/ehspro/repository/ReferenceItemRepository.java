package com.ehspro.repository;
import com.ehspro.entity.*;
import org.springframework.data.jpa.repository.*;
public interface ReferenceItemRepository extends JpaRepository<ReferenceItem, ReferenceItemId> { java.util.List<ReferenceItem> findByKindOrderByNameAsc(String kind); }

