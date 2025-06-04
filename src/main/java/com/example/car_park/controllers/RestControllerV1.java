package com.example.car_park.controllers;


import com.example.car_park.controllers.dto.response.BrandDto;
import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.controllers.dto.response.EnterpriseDto;
import com.example.car_park.service.BrandService;
import com.example.car_park.service.DriverService;
import com.example.car_park.service.EnterpriseService;
import com.example.car_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public ResponseEntity<?> getVehicles(@PathVariable(required = false) Long id) {
        if (id == null) {
            return ResponseEntity.ok(vehicleService.findAllForRest());
        }

        return ResponseEntity.ok(vehicleService.findForRest(id));
    }

    @GetMapping("/enterprises")
    public List<EnterpriseDto> getEnterprises() {
        return enterpriseService.findAllEnterprises();
    }

    @GetMapping("/drivers")
    public List<DriverDto> getDrivers() {
        return driverService.findAll();
    }
}
