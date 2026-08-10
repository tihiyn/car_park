package com.example.car_park.controllers.converters;

import com.example.car_park.controllers.dto.response.VehicleEditDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Формы создания и редактирования ТС отправляют бренд как id в теле формы
 * (option value="1"), а поле DTO имеет тип BrandEditDto.
 */
@Component
public class StringToBrandEditDtoConverter implements Converter<String, VehicleEditDto.BrandEditDto> {

    @Override
    public VehicleEditDto.BrandEditDto convert(@NonNull String source) {
        if (source.isBlank()) {
            return null;
        }
        return new VehicleEditDto.BrandEditDto().setId(Long.valueOf(source.trim()));
    }
}
