package com.example.car_park.controllers.converters;

import com.example.car_park.controllers.dto.response.VehicleEditDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToDriverEditDtoConverter implements Converter<String, VehicleEditDto.DriverEditDto> {

    @Override
    public VehicleEditDto.DriverEditDto convert(@NonNull String source) {
        if (source.isBlank()) {
            return null;
        }
        return new VehicleEditDto.DriverEditDto().setId(Long.valueOf(source.trim()));
    }
}
