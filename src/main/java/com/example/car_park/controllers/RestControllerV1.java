package com.example.car_park.controllers;


import com.example.car_park.controllers.dto.response.BrandDto;
import com.example.car_park.service.BrandService;
import com.example.car_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestControllerV1 {
    private final BrandService brandService;
    private final VehicleService vehicleService;

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

}
