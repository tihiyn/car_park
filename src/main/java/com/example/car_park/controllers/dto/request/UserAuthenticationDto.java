package com.example.car_park.controllers.dto.request;

import lombok.Data;

@Data
public class UserAuthenticationDto {
    private String username;
    private String password;
}
