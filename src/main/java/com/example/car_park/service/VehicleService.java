package com.example.car_park.service;

import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public void save(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle find(Long id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }

    public List<Vehicle> findByKeyword(String keyword) {
        return vehicleRepository.findByKeyword(keyword);
    }
}
