package com.example.car_park.controllers.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.time.ZonedDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleLocationDto {
    private Double latitude;
    private Double longitude;
    private ZonedDateTime timestamp;
    private Point geometry;
}
