package com.example.car_park.service;

import com.example.car_park.dao.UserRepository;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    // TODO: нет ли дубликата с методом из ManagerProvider?
    @Override
    @Cacheable(value = "loadUserByUsername", unless = "#result == null")
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.error("Пользователь с именем {} не найден", username);
                return new UsernameNotFoundException("Пользователь не найден");
            });
    }
}
