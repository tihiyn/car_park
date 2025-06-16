package com.example.car_park.controllers.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserRegistrationDto {
    private String surname;
    private String name;
    private BigDecimal salary;
    private String email;
    private String username;
    private String password;
}
