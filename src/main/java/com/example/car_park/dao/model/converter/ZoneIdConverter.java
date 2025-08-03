package com.example.car_park.dao.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.ZoneId;

@Converter
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

    @Override
    public String convertToDatabaseColumn(ZoneId zoneId) {
        return zoneId != null ? zoneId.getId() : null;
    }

    @Override
    public ZoneId convertToEntityAttribute(String zoneIdString) {
        return zoneIdString != null ? ZoneId.of(zoneIdString) : null;
    }
}
