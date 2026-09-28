package com.ehspro.model;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public class ObservationSubTypeTranslation {
    @Positive public Long languageId;
    public Long observationSubTypeId;
    public String subTypeDescription;
}
