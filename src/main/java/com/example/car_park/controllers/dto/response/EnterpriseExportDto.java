package com.example.car_park.controllers.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZoneId;
import java.util.List;

@Data
@Accessors(chain = true)
public class EnterpriseExportDto {
    private Long id;
    private String name;
    private String city;
    private String registrationNumber;
    private ZoneId timeZone;
    private List<VehicleExportDto> vehicles;

    @Data
    public static class VehicleExportDto {
        private Long id;
        private String regNum;
        private Integer price;
        private Integer mileage;
        private Integer productionYear;
        private String color;
        private boolean isAvailable;
        private String purchaseDatetime;
        private List<TripJsonExportDto> trips;

        @Data
        public static class TripJsonExportDto {
            private Long id;
            private String begin;
            private String end;
            private VehicleLocationExportDto beginLocation;
            private VehicleLocationExportDto endLocation;

            @Data
            public static class VehicleLocationExportDto {
                private Long id;
                private String location;
                private String timestamp;
            }
        }
    }
}
