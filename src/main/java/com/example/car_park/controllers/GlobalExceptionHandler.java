package com.example.car_park.controllers;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    // TODO проверить, будет ли работать без аргумента
    @ExceptionHandler(BadCredentialsException.class)
    public String handleBadCredentials(RedirectAttributes redirectAttributes) {
        return "redirect:/auth/login?error";
    }
}
