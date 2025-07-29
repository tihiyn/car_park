package com.example.car_park.controllers.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ManagerDto {
    private String surname;
    private String name;
    private BigDecimal salary;

    private List<Long> enterpriseIds;
    private List<Long> vehicleIds;
    private List<Long> driverIds;
}
