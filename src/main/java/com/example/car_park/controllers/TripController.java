package com.example.car_park.controllers;

import com.example.car_park.controllers.dto.response.TripsViewModel;
import com.example.car_park.dao.model.User;
import com.example.car_park.service.TripService;
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
    private final TripService tripService;

    @GetMapping("")
    public String getTrips(@AuthenticationPrincipal User user,
                           @RequestParam("id") Long vehicleId,
                           @RequestParam ZonedDateTime from,
                           @RequestParam ZonedDateTime to,
                           Model model) {
        List<TripsViewModel> trips = tripService.getTripsForUI(user, vehicleId, from, to);
        model.addAttribute("trips", trips);
        return "trips :: list";
    }
}
