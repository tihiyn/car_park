package com.example.car_park.service;

import com.example.car_park.dao.UserRepository;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final UserRepository userRepository;

    public Manager getManagerByUser(User user) {
        // TODO: подумать над исключением
        return userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден")).getManager();
    }
}
