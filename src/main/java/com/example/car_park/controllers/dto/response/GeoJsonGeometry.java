package com.example.car_park.controllers.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Геометрия GeoJSON")
public class GeoJsonGeometry {

    @Schema(description = "Тип геометрии", example = "LineString")
    private String type;

    @Schema(
            description = "Координаты в порядке [долгота, широта]",
            example = "[[45.018758, 53.195873], [45.031122, 53.212345]]"
    )
    private List<List<Double>> coordinates; // [ [lon, lat], [lon, lat], ... ]
}
