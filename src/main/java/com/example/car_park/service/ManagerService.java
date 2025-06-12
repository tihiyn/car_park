package com.example.car_park.service;

import com.example.car_park.dao.ManagerRepository;
import com.example.car_park.dao.UserRepository;
import com.example.car_park.dao.mapper.ManagerMapper;
import com.example.car_park.dao.model.Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final ManagerRepository managerRepository;
    private final ManagerMapper managerMapper;
    private final UserRepository userRepository;

    public Manager getManagerByUser(User user) {
        return userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден")).getManager();
    }
}
