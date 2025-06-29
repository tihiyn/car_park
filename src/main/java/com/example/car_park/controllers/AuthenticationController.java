package com.example.car_park.controllers;

import com.example.car_park.controllers.dto.request.UserAuthenticationDto;
import com.example.car_park.dao.model.User;
import com.example.car_park.service.AuthenticationService;
import com.example.car_park.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userAuthenticationDto", new UserAuthenticationDto());
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute UserAuthenticationDto userAuthenticationDto,
                               HttpServletResponse response) {
        User authenticatedUser = authenticationService.authenticate(userAuthenticationDto);
        String jwt = jwtService.generateToken(authenticatedUser);
        response.addCookie(jwtService.addJwtToCookie(jwt));

        return "redirect:/api/enterprises";
    }
}
