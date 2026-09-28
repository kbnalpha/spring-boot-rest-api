package com.ehspro.service;

import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    private final ObjectMapper mapper;
    public DtoMapper(ObjectMapper mapper) {
        this.mapper = mapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public <T> T map(Object source, Class<T> type) { return mapper.convertValue(source, type); }
}
