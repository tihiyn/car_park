package com.example.car_park.controllers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeoJsonGeometry {
    private String type;
    private List<List<Double>> coordinates; // [ [lon, lat], [lon, lat], ... ]
}
