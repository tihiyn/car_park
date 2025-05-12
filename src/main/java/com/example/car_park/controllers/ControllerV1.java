package com.example.car_park.controllers;

import com.example.car_park.dao.BrandRepository;
import com.example.car_park.dao.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ControllerV1 {
    private final VehicleRepository vehicleRepository;
    private final BrandRepository brandRepository;

    @GetMapping("/vehicles")
    public String getVehicles(Model model) {
        model.addAttribute("vehicles", vehicleRepository.findAll());
        return "vehicles";
    }

    @GetMapping("/brands")
    public String getBrands(Model model) {
        model.addAttribute("brands", brandRepository.findAll());
        return "brands";
    }
}
