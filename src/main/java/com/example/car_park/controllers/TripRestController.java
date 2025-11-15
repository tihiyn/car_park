package com.example.car_park.controllers;

import com.example.car_park.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripRestController {
    private final TripService tripService;

    @PostMapping("/map")
    public ResponseEntity<?> showTripsOnMap(@RequestBody List<Long> tripIds) {
        return tripService.getTripsForMap(tripIds);
    }
}
