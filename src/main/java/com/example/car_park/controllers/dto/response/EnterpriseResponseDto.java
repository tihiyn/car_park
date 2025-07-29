package com.example.car_park.controllers.dto.response;


import lombok.Data;

import java.util.List;

@Data
public class EnterpriseResponseDto {
    private Long id;
    private String name;
    private String city;
    private String registrationNumber;

    private List<Long> driverIds;
    private List<Long> vehicleIds;
}
