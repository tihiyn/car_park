package com.example.car_park.controllers.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class VehicleDto {
    private Long id;
    private String regNum;
    private Integer price;
    private Integer mileage;
    private Integer productionYear;
    private String color;
    private List<Long> driverIds;
    private Long activeDriverId;
}
