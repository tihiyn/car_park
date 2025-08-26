package com.example.car_park.controllers.dto.response;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class TripDto {
    private BeginInfo beginInfo;
    private EndInfo endInfo;

    @Data
    public static class BeginInfo {
        private String beginAddress;
        private Double beginLat;
        private Double beginLong;
        private ZonedDateTime beginTS;
    }

    @Data
    public static class EndInfo {
        private String endAddress;
        private Double endLat;
        private Double endLong;
        private ZonedDateTime endTS;
    }
}
