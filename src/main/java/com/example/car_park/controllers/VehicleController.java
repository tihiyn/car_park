package com.example.car_park.controllers;

import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping()
    public String getVehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        return "vehicles";
    }

    @GetMapping("/new")
    public String createVehicle(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "new_vehicle";
    }

    @PostMapping("/save")
    public String saveVehicle(@ModelAttribute Vehicle vehicle) {
        vehicleService.save(vehicle);
        return "redirect:/vehicles";
    }

    @GetMapping("/edit")
    public String editVehicle(@RequestParam Long id, Model model) {
        Vehicle vehicle = vehicleService.find(id);
        if (vehicle == null) {
            return "redirect:/vehicles";
        }

        model.addAttribute("vehicle", vehicle);
        return "edit_vehicle";
    }

    @GetMapping("/delete")
    public String deleteVehicle(@RequestParam Long id) {
        vehicleService.delete(id);
        return "redirect:/vehicles";
    }

    @GetMapping("/search")
    public String searchVehicle(@RequestParam String keyword, Model model) {
        model.addAttribute("vehicles", vehicleService.findByKeyword(keyword));
        return "search_vehicles";
    }
}
