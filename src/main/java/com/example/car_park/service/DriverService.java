package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.controllers.providers.ManagerProvider;
import com.example.car_park.dao.DriverRepository;
import com.example.car_park.dao.mapper.DriverMapper;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    private final DriverRepository driverRepository;
    private final ManagerProvider managerProvider;

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
        List<Driver> managedDrivers = managerProvider.getManagerByUser(user)
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

    public void checkAllExists(List<Driver> all, Set<Long> toFindIds) {
        if (all.size() == toFindIds.size()) {
            return;
        }
        Set<Long> allIds = all.stream()
            .map(Driver::getId)
            .collect(Collectors.toSet());
        Set<Long> missingIds = toFindIds.stream()
            .filter(id -> !allIds.contains(id))
            .collect(Collectors.toSet());
        throw new ResponseStatusException(NOT_FOUND,
            String.format("Водители с id %s отсутствуют", missingIds));
    }

    public void checkAllBelongs(List<Driver> managed, Set<Long> toFindIds) {
        if (managed.size() == toFindIds.size()) {
            return;
        }
        Set<Long> unmanagedIds = toFindIds.stream()
            .filter(id -> managed.stream()
                .noneMatch(d -> d.getId().equals(id)))
            .collect(Collectors.toSet());
        throw new ResponseStatusException(FORBIDDEN,
            String.format("Водители с id %s не относятся к Вашим предприятиям", unmanagedIds));
    }

    public List<Driver> findAllWithoutActiveVehicle(List<Driver> ds) {
        return ds.stream()
                .filter(driver -> driver.getActiveVehicle() == null)
                .toList();
    }

    public Driver findByIdIn(List<Driver> drivers, Long id) {
        return drivers.stream()
            .filter(d -> d.getId().equals(id))
            .findAny()
            .orElse(null);
    }

    public Driver getIfBelongs(Manager m, Long id) {
        return m.getManagedEnterprises().stream()
            .flatMap(e -> e.getDrivers().stream())
            .filter(d -> d.getId().equals(id))
            .findAny()
            .orElseThrow(() -> new ResponseStatusException(FORBIDDEN,
                String.format("Водитель с id=%d не относится к Вашим предприятиям", id)));
    }
}
