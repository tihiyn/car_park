package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.dao.DriverRepository;
import com.example.car_park.dao.mapper.DriverMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public List<DriverDto> findAll() {
        return driverRepository.findAll().stream()
                .map(driverMapper::driverToDriverDto)
                .toList();
    }
}
