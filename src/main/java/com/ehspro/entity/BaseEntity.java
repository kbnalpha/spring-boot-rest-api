package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@MappedSuperclass
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long createdBy;
    public LocalDateTime createdDate;
    public Long modifiedBy;
    public LocalDateTime modifiedDate;
    @PrePersist
    void created() { if (createdDate == null) createdDate = LocalDateTime.now(); }
}

