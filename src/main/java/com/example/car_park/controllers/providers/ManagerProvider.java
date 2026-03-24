package com.example.car_park.controllers.providers;

import com.example.car_park.dao.UserRepository;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerProvider {
    private final UserRepository ur;

    public Manager getManagerByUser(User user) {
        return ur.findByUsername(user.getUsername())
                .orElseThrow(() -> {
                    log.error("Пользователь с именем {} не найден", user.getUsername());
                    return new UsernameNotFoundException("Пользователь не найден");
                }).getManager();
    }
}
