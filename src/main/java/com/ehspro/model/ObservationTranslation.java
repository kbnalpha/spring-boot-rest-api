package com.ehspro.model;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public class ObservationTranslation {
    @NotNull @Positive public Long languageId;
    public String typeDescription;
    public Long businessUnitId;
    @Valid public List<@NotNull ObservationSubTypeTranslation> observationSubTypes = new ArrayList<>();
}
