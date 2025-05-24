package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.VehicleDto;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.mapper.VehicleMapper;
import com.example.car_park.dao.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

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

    public List<VehicleDto> findAllForRest() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::vehicleToVehicleDto)
                .collect(Collectors.toList());
    }

    public VehicleDto findForRest(Long id) {
        return vehicleMapper.vehicleToVehicleDto(vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Транспортное средство с id=%d отсутствует", id))));
    }
}
