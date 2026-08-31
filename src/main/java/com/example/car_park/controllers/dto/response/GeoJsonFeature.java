package com.example.car_park.controllers.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Schema(description = "Объект GeoJSON")
public class GeoJsonFeature {

    @Schema(description = "Тип объекта GeoJSON", example = "Feature")
    private String type;

    @Schema(
            description = "Произвольные атрибуты объекта",
            example = "{\"name\": \"1\", \"description\": \"Coordinates\"}"
    )
    private Map<String, Object> properties;

    @Schema(description = "Геометрия объекта")
    private GeoJsonGeometry geometry;
}
