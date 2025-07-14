package com.example.car_park.dao;

import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Page<Driver> findAllByEnterpriseIn(List<Enterprise> enterprises, Pageable pageable);
}
