package com.example.car_park.controllers.ui;

import com.example.car_park.controllers.dto.response.TripsViewModel;
import com.example.car_park.controllers.providers.TripProvider;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZonedDateTime;
import java.util.List;

@Controller
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripProvider tp;

    @GetMapping("")
    public String findTripsInInterval(@AuthenticationPrincipal User u,
                                      @RequestParam("vId") Long vId,
                                      @RequestParam ZonedDateTime from,
                                      @RequestParam ZonedDateTime to,
                                      Model m) {
        List<TripsViewModel> trips = tp.findInIntervalForUI(u, vId, from, to);
        System.out.printf("TripController - %d%n", System.currentTimeMillis());
        m.addAttribute("trips", trips);
        return "trips :: list";
    }
}
