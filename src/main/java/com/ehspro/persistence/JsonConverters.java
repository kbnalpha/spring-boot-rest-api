package com.ehspro.persistence;
import com.ehspro.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
public final class JsonConverters {
    private JsonConverters() {}
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    public abstract static class JsonConverter<T> implements AttributeConverter<T, String> {
        private final TypeReference<T> type;
        protected JsonConverter(TypeReference<T> type) { this.type = type; }
        public String convertToDatabaseColumn(T value) {
            if (value == null) return null;
            try { return MAPPER.writeValueAsString(value); }
            catch (Exception e) { throw new IllegalArgumentException("Cannot serialize stored JSON", e); }
        }
        public T convertToEntityAttribute(String value) {
            if (value == null) return null;
            try { return MAPPER.readValue(value, type); }
            catch (Exception e) { throw new IllegalStateException("Cannot deserialize stored JSON", e); }
        }
    }
    @Converter
    public static class TreeConverter extends JsonConverter<JsonNode> {
        public TreeConverter() { super(new TypeReference<>() {}); }
    }
    @Converter
    public static class LongListConverter extends JsonConverter<List<Long>> {
        public LongListConverter() { super(new TypeReference<>() {}); }
    }
    @Converter
    public static class NameTranslationListConverter extends JsonConverter<List<NameTranslation>> {
        public NameTranslationListConverter() { super(new TypeReference<>() {}); }
    }
    @Converter
    public static class ActivityTranslationListConverter extends JsonConverter<List<ActivityTranslation>> {
        public ActivityTranslationListConverter() { super(new TypeReference<>() {}); }
    }
    @Converter
    public static class LocationTranslationListConverter extends JsonConverter<List<LocationTranslation>> {
        public LocationTranslationListConverter() { super(new TypeReference<>() {}); }
    }
    @Converter
    public static class ObservationTranslationListConverter extends JsonConverter<List<ObservationTranslation>> {
        public ObservationTranslationListConverter() { super(new TypeReference<>() {}); }
    }
    @Converter
    public static class PermissionNodeListConverter extends JsonConverter<List<PermissionNode>> {
        public PermissionNodeListConverter() { super(new TypeReference<>() {}); }
    }
}

