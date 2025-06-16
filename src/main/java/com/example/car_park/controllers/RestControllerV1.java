package com.example.car_park.controllers;


import com.example.car_park.controllers.dto.response.BrandDto;
import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.controllers.dto.response.EnterpriseDto;
import com.example.car_park.dao.model.User;
import com.example.car_park.service.BrandService;
import com.example.car_park.service.DriverService;
import com.example.car_park.service.EnterpriseService;
import com.example.car_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestControllerV1 {
    private final BrandService brandService;
    private final VehicleService vehicleService;
    private final EnterpriseService enterpriseService;
    private final DriverService driverService;

    @GetMapping("/brands")
    public List<BrandDto> getBrands() {
        return brandService.findAllForRest();
    }

    @GetMapping({"/vehicles", "/vehicles/{id}"})
    public ResponseEntity<?> getVehicles(@AuthenticationPrincipal User user,
                                         @PathVariable(required = false) Long id) {
        if (id == null) {
            return ResponseEntity.ok(vehicleService.findAllForRest(user));
        }

        return ResponseEntity.ok(vehicleService.findForRest(id));
    }

    @GetMapping("/enterprises")
    public List<EnterpriseDto> getEnterprises(@AuthenticationPrincipal User user) {
        return enterpriseService.findAllEnterprises(user);
    }

    @GetMapping("/drivers")
    public List<DriverDto> getDrivers(@AuthenticationPrincipal User user) {
        return driverService.findAll(user);
    }
}
