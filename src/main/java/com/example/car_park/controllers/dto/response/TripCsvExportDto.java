package com.example.car_park.controllers.dto.response;

import org.locationtech.jts.geom.Point;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TripCsvExportDto(
        UUID enterpriseId,
        String name,
        String city,
        String registrationNumber,
        ZoneId timeZone,

        UUID vehicleId,
        String regNum,
        Integer price,
        Integer mileage,
        Integer productionYear,
        String color,
        boolean isAvailable,
        ZonedDateTime purchaseDatetime,

        UUID tripId,
        ZonedDateTime begin,
        ZonedDateTime end,

        UUID startVehicleLocationId,
        Point startLocation,
        ZonedDateTime startTimestamp,

        UUID endVehicleLocationId,
        Point endLocation,
        ZonedDateTime endTimestamp
) {}
