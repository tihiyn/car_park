package com.example.car_park.controllers.dto.response;

import org.locationtech.jts.geom.Point;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public record TripCsvExportDto(
        Long enterpriseId,
        String name,
        String city,
        String registrationNumber,
        ZoneId timeZone,

        Long vehicleId,
        String regNum,
        Integer price,
        Integer mileage,
        Integer productionYear,
        String color,
        boolean isAvailable,
        ZonedDateTime purchaseDatetime,

        Long tripId,
        ZonedDateTime begin,
        ZonedDateTime end,

        Long startVehicleLocationId,
        Point startLocation,
        ZonedDateTime startTimestamp,

        Long endVehicleLocationId,
        Point endLocation,
        ZonedDateTime endTimestamp
) {}
