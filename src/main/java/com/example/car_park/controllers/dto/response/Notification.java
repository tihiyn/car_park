package com.example.car_park.controllers.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
public class Notification {
    private Double beginLon;
    private Double beginLat;
    private Double endLon;
    private Double endLat;
    private String enterprise;
    private String regNum;
    private Set<String> managers;
}
