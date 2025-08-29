package com.example.car_park.controllers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GeoJsonFeature {
    private String type;
    private Map<String, Object> properties;
    private GeoJsonGeometry geometry;
}
