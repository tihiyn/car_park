package com.example.car_park.controllers.dto.response;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class VehicleEditDto {
    private Long id;
    private String regNum;
    private Integer price;
    private Integer mileage;
    private Integer productionYear;
    private String color;
    private boolean isAvailable;
    private ZonedDateTime purchaseDatetime;
    private BrandEditDto brand;
    private DriverEditDto activeDriver;
    private List<DriverEditDto> drivers;

    @Data
    @Accessors(chain = true)
    public static class BrandEditDto {
        private Long id;
        private String name;
    }

    @Data
    @Accessors(chain = true)
    public static class DriverEditDto {
        private Long id;
        private String name;
    }
}
