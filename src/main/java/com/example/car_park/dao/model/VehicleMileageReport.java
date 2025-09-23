package com.example.car_park.dao.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class VehicleMileageReport extends Report<Map<String, Long>> {
    private Long vehicleId;
}
