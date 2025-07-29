package com.example.car_park.controllers.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class VehicleResponseDto {
    private Long id;
    private String regNum;
    private Integer price;
    private Integer mileage;
    private Integer productionYear;
    private String color;
    private boolean isAvailable;

    private Long brandId;
    private Long enterpriseId;
    private Long activeDriverId;

    private List<Long> driverIds;
}
