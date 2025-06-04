package com.example.car_park.controllers.dto.response;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DriverDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String driverLicense;
    private BigDecimal salary;
    private String phoneNumber;
    private List<Long> vehicleIds;
    private Long activeVehicleId;
}
