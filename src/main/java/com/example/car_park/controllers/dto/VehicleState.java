package com.example.car_park.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleState {
    private double lat;
    private double lon;
    private double bearing; // направление, градусов
    private double speed;   // скорость, км/ч
}
