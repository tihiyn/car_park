package com.example.car_park.controllers.dto.response;

import lombok.Data;

@Data
public class BrandDto {
    private String name;
    private String type;
    private String transmission;
    private Double engineVolume;
    private Integer enginePower;
    private Integer numOfSeats;
}
