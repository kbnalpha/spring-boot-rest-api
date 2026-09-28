package com.ehspro.model;
import java.time.LocalTime;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
public class ShiftTiming {
    @NotBlank @Size(max=100) public String name;
    @NotNull public LocalTime startTime;
    @NotNull public LocalTime endTime;
    @AssertTrue(message="shift start and end times must differ") @JsonIgnore
    public boolean isDurationValid() { return startTime==null || endTime==null || !startTime.equals(endTime); }
}
