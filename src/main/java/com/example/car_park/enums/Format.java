package com.example.car_park.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Format {
    JSON("json"),
    GEO_JSON("geoJson");

    private final String value;

    public static Format getByValue(String value) {
        for (Format format : values()) {
            if (format.getValue().equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Неизвестный формат: " + value);
    }
}
