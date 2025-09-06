package com.example.car_park.dao;

import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    Page<Enterprise> findAllByManagersContaining(Manager managers, Pageable pageable);
    Optional<Enterprise> findByRegistrationNumber(String registrationNumber);
}
