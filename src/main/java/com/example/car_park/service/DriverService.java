package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.dao.DriverRepository;
import com.example.car_park.dao.mapper.DriverMapper;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverMapper driverMapper;
    private final DriverRepository driverRepository;
    private final ManagerService managerService;

    public List<DriverDto> findAllForRest(User user) {
        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .flatMap(enterprise -> enterprise.getDrivers().stream().map(driverMapper::driverToDriverDto))
                .toList();
    }

    public List<Driver> findAllByIds(User user, Set<Long> driverIds) {
        List<Driver> allDrivers = driverRepository.findAllById(driverIds);

        if (allDrivers.size() != driverIds.size()) {
            Set<Long> foundedDriverIds = allDrivers.stream()
                    .map(Driver::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingDriverIds = driverIds.stream()
                    .filter(id -> !foundedDriverIds.contains(id))
                    .collect(Collectors.toSet());

            throw new ResponseStatusException(NOT_FOUND,
                    String.format("Водители с id %s отсутствуют", missingDriverIds));
        }

        List<Driver> managedDrivers = managerService.getManagerByUser(user)
                .getManagedEnterprises().stream()
                .flatMap(enterprise -> enterprise.getDrivers().stream())
                .filter(driver -> driverIds.contains(driver.getId()))
                .toList();

        if (managedDrivers.size() != driverIds.size()) {
            Set<Long> unmanagedDriverIds = driverIds.stream()
                    .filter(id -> managedDrivers.stream()
                            .noneMatch(d -> d.getId().equals(id)))
                    .collect(Collectors.toSet());

            throw new ResponseStatusException(FORBIDDEN,
                    String.format("Водители с id %s не относятся к Вашим предприятиям", unmanagedDriverIds));
        }

        return managedDrivers;
    }
}
