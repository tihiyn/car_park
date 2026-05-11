package com.example.car_park.controllers.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/cpu")
    public Map<String, String> cpu() {
        return Map.of(
            "HOSTNAME", System.getenv().getOrDefault("HOSTNAME", "unknown")
        );
    }
}
