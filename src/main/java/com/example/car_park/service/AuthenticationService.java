package com.example.car_park.service;

import com.example.car_park.controllers.dto.request.UserAuthenticationDto;
import com.example.car_park.controllers.dto.request.UserRegistrationDto;
import com.example.car_park.dao.UserRepository;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public void register(UserRegistrationDto userRegistrationDto) {
        if (userRepository.findByUsername(userRegistrationDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        Manager manager = new Manager()
                .setName(userRegistrationDto.getName())
                .setSurname(userRegistrationDto.getSurname())
                .setSalary(userRegistrationDto.getSalary())
                .setEmail(userRegistrationDto.getEmail());
        User user = new User()
                .setUsername(userRegistrationDto.getUsername())
                .setPassword(passwordEncoder.encode(userRegistrationDto.getPassword()))
                .setManager(manager);

        userRepository.save(user);
    }

    public User authenticate(UserAuthenticationDto userAuthenticationDto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userAuthenticationDto.getUsername(),
                userAuthenticationDto.getPassword()));

        return userRepository.findByUsername(userAuthenticationDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
