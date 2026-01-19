package com.example.car_park.controllers.ui;

import com.example.car_park.controllers.providers.EnterpriseProvider;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/ui/enterprises")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class EnterpriseController {
    private final EnterpriseProvider ep;

    @GetMapping("")
    public String getEnterprises(@AuthenticationPrincipal User u,
                                 Model m,
                                 @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable p) {
        m.addAttribute("enterprises", ep.findAllForUI(u, p));
        m.addAttribute("timeZones", ep.getTimeZones());
        return "enterprises";
    }

    @PostMapping("/update-timezone")
    public String updateTimeZone(@AuthenticationPrincipal User u,
                                 @RequestParam Long eId,
                                 @RequestParam String timeZone) {
        ep.updateTimeZone(u, eId, timeZone);
        return "redirect:/api/ui/enterprises";
    }
}
