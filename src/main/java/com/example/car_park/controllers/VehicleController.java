package com.example.car_park.controllers;

import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.service.BrandService;
import com.example.car_park.service.DriverService;
import com.example.car_park.service.EnterpriseService;
import com.example.car_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;
    private final BrandService brandService;
    private final DriverService driverService;
    private final EnterpriseService enterpriseService;

    @GetMapping()
    public String getVehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        return "vehicles";
    }

    @GetMapping("/new")
    public String createVehicle(@AuthenticationPrincipal User user,
                                @RequestParam Long enterpriseId,
                                Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("enterprise", enterpriseService.findById(user, enterpriseId));
        model.addAttribute("drivers", driverService.findAll(user, enterpriseId));
        model.addAttribute("activeDriverPretendents", driverService.findActiveDriverPretendents(user, enterpriseId));
        return "new_vehicle";
    }

    @PostMapping("/save")
    public String saveVehicle(@ModelAttribute Vehicle vehicle) {
        vehicleService.save(vehicle);
        return "redirect:/api/ui/enterprises";
    }

    @GetMapping("/edit/{id}")
    public String editVehicle(@AuthenticationPrincipal User user,
                              @PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        if (vehicle == null) {
            return "redirect:/api/ui/enterprises";
        }
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("enterprise", vehicle.getEnterprise());
        model.addAttribute("drivers", driverService.findAll(user, vehicle.getEnterprise().getId()));
        List<Driver> activeDriverPretendents = new ArrayList<>(driverService.findActiveDriverPretendents(user, vehicle.getEnterprise().getId()));
        activeDriverPretendents.add(vehicle.getActiveDriver());
        model.addAttribute("activeDriverPretendents", activeDriverPretendents);
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
