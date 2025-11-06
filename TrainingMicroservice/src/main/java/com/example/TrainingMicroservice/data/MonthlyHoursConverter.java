package com.example.TrainingMicroservice.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter
public class MonthlyHoursConverter implements AttributeConverter<Map<Integer, Map<Integer, Integer>>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<Integer, Map<Integer, Integer>> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting monthlyHours to JSON", e);
        }
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading monthlyHours from JSON", e);
        }
    }
}
