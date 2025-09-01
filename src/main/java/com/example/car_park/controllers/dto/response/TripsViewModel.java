package com.example.car_park.controllers.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class TripsViewModel {
    private Long id;
    private String beginAddress;
    private ZonedDateTime beginTS;
    private String endAddress;
    private ZonedDateTime endTS;
}
