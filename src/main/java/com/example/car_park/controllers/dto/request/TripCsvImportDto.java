package com.example.car_park.controllers.dto.request;

import lombok.Data;

import java.time.ZoneId;
import java.lang.String;

@Data
public class TripCsvImportDto {
    private Long enterpriseId;
    private String name;
    private String city;
    private String registrationNumber;
    private ZoneId timeZone;

    private Long vehicleId;
    private String regNum;
    private Integer price;
    private Integer mileage;
    private Integer productionYear;
    private String color;
    private boolean available;
    private String purchaseDatetime;

    private Long tripId;
    private String begin;
    private String end;

    private Long startVehicleLocationId;
    private String startLocation;
    private String startTimestamp;

    private Long endVehicleLocationId;
    private String endLocation;
    private String endTimestamp;
}

