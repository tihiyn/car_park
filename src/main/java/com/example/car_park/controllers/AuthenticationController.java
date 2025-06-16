package com.example.car_park.controllers;

import com.example.car_park.controllers.dto.request.UserAuthenticationDto;
import com.example.car_park.controllers.dto.request.UserRegistrationDto;
import com.example.car_park.dao.model.User;
import com.example.car_park.service.AuthenticationService;
import com.example.car_park.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

//    @PostMapping("/register")
//    public String register(@ModelAttribute("manager") UserRegistrationDto userRegistrationDto,
//                           Model model) {
//        try {
//            authenticationService.register(userRegistrationDto);
//            return "redirect:/login?registered";
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("error", e.getMessage());
//            return "register";
//        }
//    }

    @GetMapping("/auth/login")
    public String login(Model model) {
        model.addAttribute("userAuthenticationDto", new UserAuthenticationDto());
        return "login";
    }

    @PostMapping("/auth/login")
    public String authenticate(@ModelAttribute UserAuthenticationDto userAuthenticationDto) {
        User authenticatedUser = authenticationService.authenticate(userAuthenticationDto);
        String jwt = jwtService.generateToken(authenticatedUser);
//        Cookie cookie = new Cookie("JWT", jwt);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        response.addCookie(cookie);

        return jwt;
    }

}
