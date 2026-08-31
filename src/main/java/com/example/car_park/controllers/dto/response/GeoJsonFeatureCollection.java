package com.example.car_park.controllers.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Коллекция объектов GeoJSON")
public class GeoJsonFeatureCollection {

    @Schema(description = "Тип объекта GeoJSON", example = "FeatureCollection")
    private final String type;

    @ArraySchema(arraySchema = @Schema(description = "Объекты коллекции: по одному на поездку"))
    private List<GeoJsonFeature> features;
}
