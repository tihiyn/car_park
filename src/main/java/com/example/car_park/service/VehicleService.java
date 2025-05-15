package com.example.car_park.service;

import com.example.car_park.dao.BrandRepository;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.model.Brand;
import com.example.car_park.dao.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final BrandRepository brandRepository;

    public void save(Vehicle vehicle) {
        Brand brand = brandRepository.findBrandByNameAndTypeAndTransmissionAndEngineVolumeAndEnginePowerAndNumOfSeats(
                        vehicle.getBrand().getName(),
                        vehicle.getBrand().getType(),
                        vehicle.getBrand().getTransmission(),
                        vehicle.getBrand().getEngineVolume(),
                        vehicle.getBrand().getEnginePower(),
                        vehicle.getBrand().getNumOfSeats())
                .orElseGet(() -> brandRepository.save(vehicle.getBrand()));

        vehicle.setBrand(brand);
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
