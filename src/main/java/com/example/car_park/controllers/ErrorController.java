package com.example.car_park.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/forbidden")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "У вас нет доступа к этой странице.");
        return "forbidden";
    }
}

