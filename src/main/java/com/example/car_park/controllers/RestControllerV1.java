package com.example.car_park.controllers;


import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.dao.model.User;
import com.example.car_park.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestControllerV1 {
    private final DriverService driverService;

    // TODO: сделать контроллер для водителей с CRUD
    @GetMapping("/drivers")
    public List<DriverDto> getDrivers(@AuthenticationPrincipal User user) {
        return driverService.findAllForRest(user);
    }
}
