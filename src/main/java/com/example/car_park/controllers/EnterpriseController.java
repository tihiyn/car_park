package com.example.car_park.controllers;

import com.example.car_park.dao.model.User;
import com.example.car_park.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/ui/enterprises")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class EnterpriseController {
    private final EnterpriseService enterpriseService;

    @GetMapping("")
    public String getEnterprises(@AuthenticationPrincipal User user,
                                 Model model,
                                 @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        model.addAttribute("enterprises", enterpriseService.findAll(user, pageable));
        return "enterprises";
    }
}
