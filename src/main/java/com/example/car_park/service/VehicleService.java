package com.example.car_park.service;

import com.example.car_park.controllers.dto.VehicleState;
import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.VehicleLocationJsonDto;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.mapper.VehicleMapper;
import com.example.car_park.dao.model.Brand;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final ManagerService managerService;
    private final BrandService brandService;
    private final EnterpriseService enterpriseService;
    private final DriverService driverService;
    private final Random random = new Random();


    public VehicleService(VehicleRepository vehicleRepository,
                          VehicleMapper vehicleMapper,
                          ManagerService managerService, BrandService brandService,
                          @Lazy EnterpriseService enterpriseService,
                          DriverService driverService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
        this.managerService = managerService;
        this.brandService = brandService;
        this.enterpriseService = enterpriseService;
        this.driverService = driverService;
    }

    public Vehicle getIfBelongs(Manager m, Long id) {
        return m.getManagedEnterprises().stream()
            .flatMap(enterprise -> enterprise.getVehicles().stream())
            .filter(v -> v.getId().equals(id))
            .findAny()
            .orElseThrow(() -> new ResponseStatusException(FORBIDDEN,
                String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", id)));
    }

    public List<Vehicle> findAllByManager(Manager m) {
        return m.getManagedEnterprises()
            .stream()
            .flatMap(e -> e.getVehicles().stream())
            .toList();
    }


    public List<Vehicle> findAllByIds(User user, Set<Long> vehicleIds) {
        List<Vehicle> allVehicles = vehicleRepository.findAllById(vehicleIds);
        if (allVehicles.size() != vehicleIds.size()) {
            Set<Long> foundedVehicleIds = allVehicles.stream()
                    .map(Vehicle::getId)
                    .collect(Collectors.toSet());
            Set<Long> missingVehicleIds = vehicleIds.stream()
                    .filter(id -> !foundedVehicleIds.contains(id))
                    .collect(Collectors.toSet());
            throw new ResponseStatusException(NOT_FOUND,
                    String.format("Транспортные средства с id %s отсутствуют", missingVehicleIds));
        }
        List<Vehicle> managedVehicles = managerService.getManagerByUser(user)
                .getManagedEnterprises().stream()
                .flatMap(enterprise -> enterprise.getVehicles().stream())
                .filter(vehicle -> vehicleIds.contains(vehicle.getId()))
                .toList();
        if (managedVehicles.size() != vehicleIds.size()) {
            Set<Long> unmanagedVehicleIds = vehicleIds.stream()
                    .filter(id -> managedVehicles.stream()
                            .noneMatch(v -> v.getId().equals(id)))
                    .collect(Collectors.toSet());
            throw new ResponseStatusException(FORBIDDEN,
                    String.format("Транспортные средства с id %s не относятся к Вашим предприятиям", unmanagedVehicleIds));
        }
        return managedVehicles;
    }

    public VehicleResponseDto findByIdForRest(User user, Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Транспортное средство с id=%d отсутствует", id)));
        return vehicleMapper.vehicleToVehicleResponseDto(managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .flatMap(enterprise -> enterprise.getVehicles().stream())
                .filter(v -> v.getId().equals(vehicle.getId()))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN,
                        String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", id))));
    }

    public List<VehicleResponseDto> findAllForRest(User user, Pageable pageable) {
        List<VehicleResponseDto> list = vehicleRepository.findAllByEnterpriseIn(managerService.getManagerByUser(user).getManagedEnterprises(), pageable).getContent()
                .stream()
                .map(vehicleMapper::vehicleToVehicleResponseDto)
                .toList();
        System.out.println(list.size());
        return list;
        // TODO удалить устаревший вариант
//        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
//                .flatMap(enterprise -> enterprise.getVehicles().stream().map(vehicleMapper::vehicleToVehicleResponseDto))
//
//                .toList();
    }

    public Vehicle create(User user, VehicleRequestDto vehicleRequestDto) {
        Brand brand = brandService.findById(vehicleRequestDto.getBrandId());
        Enterprise enterprise = enterpriseService.findById(user, vehicleRequestDto.getEnterpriseId());
        List<Driver> drivers = driverService.findAllByIds(user, vehicleRequestDto.getDriverIds());
        // TODO: перенести эту проверку на этап валидации входных данных
        if (vehicleRequestDto.getActiveDriverId() != null &&
                !vehicleRequestDto.getDriverIds().contains(vehicleRequestDto.getActiveDriverId())) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Активный водитель должен быть из списка назначенных водителей");
        }
        // TODO: проверить, что будет если назначить водителя, который активен на другой машине
        Driver activeDriver = drivers.stream()
                .filter(driver -> driver.getId().equals(vehicleRequestDto.getActiveDriverId()))
                .findAny()
                .orElse(null);

        return vehicleRepository.save(vehicleMapper.vehicleRequestDtoToVehicle(vehicleRequestDto, brand, enterprise, drivers, activeDriver));
    }

    public VehicleResponseDto update(User user, Long id, VehicleRequestDto vehicleRequestDto) {
        Vehicle oldVehicle = findById(user, id);
        vehicleMapper.vehicleRequestDtoToVehicle(vehicleRequestDto, oldVehicle);
        if (!oldVehicle.getBrand().getId().equals(vehicleRequestDto.getBrandId())) {
            Brand brand = brandService.findById(vehicleRequestDto.getBrandId());
            oldVehicle.setBrand(brand);
        }
        if (!oldVehicle.getEnterprise().getId().equals(vehicleRequestDto.getEnterpriseId())) {
            Enterprise enterprise = enterpriseService.findById(user, vehicleRequestDto.getEnterpriseId());
            oldVehicle.setEnterprise(enterprise);
        }
        if (!oldVehicle.getDrivers().stream()
                .map(Driver::getId)
                .collect(Collectors.toSet()).equals(vehicleRequestDto.getDriverIds())) {
            List<Driver> updatedDriverList = driverService.findAllByIds(user, vehicleRequestDto.getDriverIds());
            oldVehicle.getDrivers().clear();
            oldVehicle.getDrivers().addAll(updatedDriverList);
        }
        // TODO: перенести эту проверку на этап валидации входных данных
        if (vehicleRequestDto.getActiveDriverId() != null &&
                !vehicleRequestDto.getDriverIds().contains(vehicleRequestDto.getActiveDriverId())) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Активный водитель должен быть из списка назначенных водителей");
        }
        // TODO: проверить, что будет если назначить водителя, который активен на другой машине
        if (!oldVehicle.getActiveDriver().getId().equals(vehicleRequestDto.getActiveDriverId())) {
            Driver activeDriver = oldVehicle.getDrivers().stream()
                    .filter(driver -> driver.getId().equals(vehicleRequestDto.getActiveDriverId()))
                    .findAny()
                    .orElse(null);
            oldVehicle.setActiveDriver(activeDriver);
        }
        return vehicleMapper.vehicleToVehicleResponseDto(vehicleRepository.save(oldVehicle));
    }

    public void delete(User user, Long id) {
        Vehicle vehicleToDelete = findById(user, id);
        vehicleToDelete.getBrand().getVehicles().remove(vehicleToDelete);
        vehicleToDelete.getEnterprise().getVehicles().remove(vehicleToDelete);
        vehicleToDelete.getDrivers().forEach(driver -> driver.getVehicles().remove(vehicleToDelete));
        vehicleRepository.delete(vehicleToDelete);
    }

    public void save(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }

    public List<Vehicle> findByKeyword(String keyword) {
        return vehicleRepository.findByKeyword(keyword);
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

    public List<Vehicle> findAllByUser(User user) {
        List<Enterprise> enterprises = managerService.getManagerByUser(user).getManagedEnterprises();
        return vehicleRepository.findAllByEnterpriseIn(enterprises);
    }

    public Flux<VehicleLocationJsonDto> streamVehicleLocation(Long vehicleId) {
        // стартовая точка (можешь заменить на реальные координаты)
        double[] start = genRandomStartPoint();
        VehicleState s = new VehicleState(
            start[0],
            start[1],
            random.nextDouble() * 360,      // направление
            40 + random.nextDouble() * 40   // скорость 40–80 км/ч
        );
        return Flux.interval(Duration.ofSeconds(1))
            .map(tick -> {
                // Немного меняем курс (+/- 5°)
                s.setBearing(s.getBearing() + (random.nextDouble() * 10 - 5));

                // Иногда — поворот на 40–90°
                if (random.nextDouble() < 0.02) {
                    s.setBearing(s.getBearing() + 40 + random.nextDouble() * 50);
                }

                // Нормализуем курс
                if (s.getBearing() < 0) s.setBearing(s.getBearing() + 360);
                if (s.getBearing() >= 360) s.setBearing(s.getBearing()- 360);

                // Перевод скорости км/ч → метры/сек
                double speedMs = s.getSpeed() / 3.6;

                // За 1 секунду автомобиль проходит:
                double distance = speedMs; // метры

                // Вычисляем новую координату по направлению
                double R = 6371000; // радиус Земли

                double latRad = Math.toRadians(s.getLat());
                double lonRad = Math.toRadians(s.getLon());
                double bearingRad = Math.toRadians(s.getBearing());

                double newLat = Math.asin(
                    Math.sin(latRad) * Math.cos(distance / R) +
                        Math.cos(latRad) * Math.sin(distance / R) * Math.cos(bearingRad)
                );

                double newLon = lonRad + Math.atan2(
                    Math.sin(bearingRad) * Math.sin(distance / R) * Math.cos(latRad),
                    Math.cos(distance / R) - Math.sin(latRad) * Math.sin(newLat)
                );

                // Обновляем состояние
                s.setLat(Math.toDegrees(newLat));
                s.setLon(Math.toDegrees(newLon));

                return new VehicleLocationJsonDto()
                    .setLatitude(s.getLat())
                    .setLongitude(s.getLon())
                    .setTimestamp(ZonedDateTime.now());
            });
    }

    private double[] genRandomStartPoint() {
        double latMin = 55.489;
        double latMax = 56.009;
        double lonMin = 37.319;
        double lonMax = 37.945;

        double lat = latMin + (latMax - latMin) * random.nextDouble();
        double lon = lonMin + (lonMax - lonMin) * random.nextDouble();

        return new double[]{lat, lon};
    }
}
