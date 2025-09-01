package com.example.car_park.controllers.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class VehicleInfoViewModel {
    private Long id;
    private String regNum;
    private Integer price;
    private Integer mileage;
    private Integer productionYear;
    private String color;
    private String isAvailable;
    private ZonedDateTime purchaseDatetime;
    private String brand;
    private String activeDriver;
    private String drivers;
}
