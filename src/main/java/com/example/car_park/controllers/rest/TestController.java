package com.example.car_park.controllers.rest;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Hidden
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
