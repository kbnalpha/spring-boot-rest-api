package com.ehspro.dto;
import java.time.LocalDateTime;
import jakarta.validation.constraints.PositiveOrZero;
public abstract class BaseDto {
    @PositiveOrZero public Long id;
    public Long createdBy;
    public LocalDateTime createdDate;
    public Long modifiedBy;
    public LocalDateTime modifiedDate;
}

