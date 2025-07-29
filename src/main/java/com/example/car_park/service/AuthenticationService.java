package com.example.car_park.service;

import com.example.car_park.controllers.dto.request.UserAuthenticationDto;
import com.example.car_park.dao.UserRepository;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public User authenticate(UserAuthenticationDto userAuthenticationDto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userAuthenticationDto.getUsername(),
                userAuthenticationDto.getPassword()));
        return userRepository.findByUsername(userAuthenticationDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
