package com.example.car_park.controllers.converters;

import com.example.car_park.enums.Period;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPeriodConverter implements Converter<String, Period> {

    @Override
    public Period convert(String source) {
        return switch (source.trim().toLowerCase()) {
            case "day" -> Period.DAY;
            case "month" -> Period.MONTH;
            case "year" -> Period.YEAR;
            default -> throw new IllegalArgumentException("Unknown period value: " + source);
        };
    }
}

