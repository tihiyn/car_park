package com.example.car_park.controllers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeoJsonFeatureCollection {
    private final String type;
    private List<GeoJsonFeature> features;
}

