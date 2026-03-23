package com.example.car_park.controllers.ui;

import com.example.car_park.controllers.dto.response.VehicleCreateDto;
import com.example.car_park.controllers.dto.response.VehicleEditDto;
import com.example.car_park.dao.model.User;
import com.example.car_park.controllers.providers.VehicleProvider;
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

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleProvider vp;

    @GetMapping("/{id}")
    public String findById(@AuthenticationPrincipal User u,
                           @PathVariable(required = false) Long id,
                           Model m) {
        System.out.printf("findById - %d%n", System.currentTimeMillis());
        m.addAttribute("vehicle", vp.findByIdForUI(u, id));
        System.out.printf("findById - %d%n", System.currentTimeMillis());
        return "vehicle_info";
    }

    @GetMapping("/new")
    public String create(@AuthenticationPrincipal User u,
                         @RequestParam Long eId,
                         Model m) {
        m.addAttribute("vehicle", vp.prepareToCreate(u, eId));
        m.addAttribute("brands", vp.getBrands());
        m.addAttribute("drivers", vp.findAllDriversByEnterprise(eId));
        m.addAttribute("activeDriverPretendents", vp.findAllDriversByEnterpriseWithoutActiveVehicle(eId));
        return "new_vehicle";
    }

    @GetMapping("/edit/{id}")
    public String edit(@AuthenticationPrincipal User u,
                       @PathVariable Long id,
                       Model m) {
        VehicleEditDto v = vp.edit(u, id);
        if (v == null) {
            return "redirect:/api/ui/enterprises";
        }
        m.addAttribute("vehicle", v);
        m.addAttribute("brands", vp.getBrands());
        m.addAttribute("drivers", vp.findAllDriversFromEnterprise(v.getId()));
        m.addAttribute("activeDriverPretendents", vp.findAllDriversWithoutActiveVehicle(v.getId()));
        return "edit_vehicle";
    }

    @PostMapping("/save")
    public String save(@AuthenticationPrincipal User u,
                       @ModelAttribute VehicleCreateDto v) {
        vp.save(u, v);
        return "redirect:/api/ui/enterprises";
    }

    @PostMapping("/update")
    public String update(@AuthenticationPrincipal User u,
                         @ModelAttribute VehicleEditDto v) {
        vp.update(u, v);
        return "redirect:/api/ui/enterprises";
    }

    @GetMapping("/delete")
    public String delete(@AuthenticationPrincipal User u,
                         @RequestParam Long id) {
        vp.delete(u, id);
        return "redirect:/vehicles";
    }

    @GetMapping("/{id}/online-map")
    public String vehicleMap(@PathVariable Long id, Model model) {
        model.addAttribute("vehicleId", id);
        return "online-map";
    }
}
