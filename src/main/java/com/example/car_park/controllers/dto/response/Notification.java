package com.example.car_park.controllers.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
public class Notification {
    private String start;
    private String finish;
    private String enterprise;
    private String regNum;
    private Set<String> managers;
}
