package com.example.car_park.controllers.dto.response;

import lombok.Data;

@Data
public class VehicleDto {
    private String regNum;
    private Integer price;
    private Integer mileage;
    private Integer productionYear;
    private String color;
    private boolean isAvailable;
    private Long brandId;
}
