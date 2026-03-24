package com.example.car_park.service;

import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {
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
        log.error("Водители с id {} отсутствуют", missingIds);
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
        log.error("Водители с id {} не относятся к Вашим предприятиям", unmanagedIds);
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
            .orElseThrow(() -> {
                log.error("Водитель с id={} не относится к предприятиям менеджера с id={}", id, m.getId());
                return new ResponseStatusException(FORBIDDEN,
                    String.format("Водитель с id=%d не относится к Вашим предприятиям", id));
            });
    }
}
