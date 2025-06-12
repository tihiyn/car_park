package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.dao.mapper.DriverMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverMapper driverMapper;
    private final ManagerService managerService;

    public List<DriverDto> findAll(User user) {
        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .flatMap(enterprise -> enterprise.getDrivers().stream().map(driverMapper::driverToDriverDto))
                .toList();
    }
}
