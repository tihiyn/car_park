package com.example.car_park.controllers.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleLocationJsonDto {
    private Double latitude;
    private Double longitude;
    private ZonedDateTime timestamp;
}
