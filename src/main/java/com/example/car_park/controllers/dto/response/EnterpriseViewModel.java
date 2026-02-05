package com.example.car_park.controllers.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
public class EnterpriseViewModel {
    private Long id;
    private String name;
    private String city;
    private String registrationNumber;
    private String timeZone;
    private List<DriverInfoViewModel> drivers;
    private List<VehicleViewModel> vehicles;

    @Data
    @Accessors(chain = true)
    public static class DriverInfoViewModel {
        private Long id;
        private String name;
    }

    @Data
    @Accessors(chain = true)
    public static class VehicleViewModel {
        private Long id;
        private String regNum;
    }
}
