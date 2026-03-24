package com.example.car_park.service;

import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
public class VehicleService {
    public Vehicle getIfBelongs(Manager m, Long id) {
        return m.getManagedEnterprises().stream()
            .flatMap(e -> e.getVehicles().stream())
            .filter(v -> v.getId().equals(id))
            .findAny()
            .orElseThrow(() -> {
                log.error("Транспортное средство с id={} не относится к предприятиям менеджера с id={}", id, m.getId());
                return new ResponseStatusException(FORBIDDEN,
                    String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", id));
            });
    }

    public List<Vehicle> findAllByManager(Manager m) {
        return m.getManagedEnterprises()
            .stream()
            .flatMap(e -> e.getVehicles().stream())
            .toList();
    }

    public void checkAllExists(List<Vehicle> all, Set<Long> toFindIds) {
        if (all.size() == toFindIds.size()) {
            return;
        }
        Set<Long> allIds = all.stream()
            .map(Vehicle::getId)
            .collect(Collectors.toSet());
        Set<Long> missingIds = toFindIds.stream()
            .filter(id -> !allIds.contains(id))
            .collect(Collectors.toSet());
        log.error("Транспортные средства с id {} отсутствуют", missingIds);
        throw new ResponseStatusException(NOT_FOUND,
            "Транспортные средства с id %s отсутствуют".formatted(missingIds));
    }

    public void checkAllBelongs(List<Vehicle> managed, Set<Long> toFindIds) {
        if (managed.size() == toFindIds.size()) {
            return;
        }
        Set<Long> unmanagedIds = toFindIds.stream()
            .filter(id -> managed.stream()
                .noneMatch(v -> v.getId().equals(id)))
            .collect(Collectors.toSet());
        log.error("Транспортные средства с id {} не относятся к Вашим предприятиям", unmanagedIds);
        throw new ResponseStatusException(FORBIDDEN,
            "Транспортные средства с id %s не относятся к Вашим предприятиям".formatted(unmanagedIds));
    }

//    public List<VehicleLocationJsonDto> getTrack(User user, Long id, ZonedDateTime begin, ZonedDateTime end, String format) {
//        // TODO заменить на findById(user, id)
//        Vehicle vehicle = findById(id);
//        ZoneId timeZone = vehicle.getEnterprise().getTimeZone();
//        List<VehicleLocation> locations = vehicleLocationRepository.findVehicleLocationsByVehicleAndTimestampBetween(
//                vehicle,
//                begin.withZoneSameInstant(ZoneId.of("UTC")),
//                end.withZoneSameInstant(ZoneId.of("UTC"))
//        );
//        return locations.stream()
//                .map(location -> vehicleLocationMapper.vehicleLocationToVehicleLocationJsonDto(location, timeZone, format))
//                .toList();
//    }
}
