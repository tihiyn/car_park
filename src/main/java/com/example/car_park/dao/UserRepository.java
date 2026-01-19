package com.example.car_park.dao;

import com.example.car_park.dao.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Cacheable(value = "findByUsername", unless = "#result == null")
    Optional<User> findByUsername(String username);
}
