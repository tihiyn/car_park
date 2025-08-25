package com.example.car_park.service;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.VehicleLocationRepository;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.mapper.TripMapper;
import com.example.car_park.dao.mapper.VehicleLocationMapper;
import com.example.car_park.dao.mapper.VehicleMapper;
import com.example.car_park.dao.model.Brand;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import com.example.car_park.enums.Format;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleLocationRepository vehicleLocationRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleLocationMapper vehicleLocationMapper;
    private final ManagerService managerService;
    private final BrandService brandService;
    private final EnterpriseService enterpriseService;
    private final DriverService driverService;
    private final TripRepository tripRepository;
    private final TripMapper tripMapper;


    public VehicleService(VehicleRepository vehicleRepository, VehicleLocationRepository vehicleLocationRepository,
                          VehicleMapper vehicleMapper, VehicleLocationMapper vehicleLocationMapper,
                          ManagerService managerService, BrandService brandService, @Lazy EnterpriseService enterpriseService,
                          DriverService driverService, TripRepository tripRepository, TripMapper tripMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleLocationRepository = vehicleLocationRepository;
        this.vehicleMapper = vehicleMapper;
        this.vehicleLocationMapper = vehicleLocationMapper;
        this.managerService = managerService;
        this.brandService = brandService;
        this.enterpriseService = enterpriseService;
        this.driverService = driverService;
        this.tripRepository = tripRepository;
        this.tripMapper = tripMapper;
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    public Vehicle findById(User user, Long id) {
        boolean isVehicleExist = vehicleRepository.findById(id).isPresent();
        if (!isVehicleExist) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Транспортное средство с id=%d отсутствует", id));
        }
        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .flatMap(enterprise -> enterprise.getVehicles().stream())
                .filter(v -> v.getId().equals(id))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN,
                        String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", id)));
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

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
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
//        // TODO замеенить на findById(user, id)
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

    public ResponseEntity<?> getTripsByPointsForAPI(User user, Long id, ZonedDateTime begin, ZonedDateTime end, Format format) {
        Vehicle vehicle = findById(user, id);
        List<VehicleLocation> locations = getTripsByPoints(vehicle, begin, end);
        ZoneId timeZone = vehicle.getEnterprise().getTimeZone();
        if (format == Format.JSON) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(locations.stream()
                            .map(location -> vehicleLocationMapper.vehicleLocationToVehicleLocationJsonDto(location, timeZone))
                            .toList()
                    );
        }
        return ResponseEntity.ok()
                .body(vehicleLocationMapper.vehicleLocationsToGeoJsonMap(locations, timeZone));
    }

    public List<VehicleLocation> getTripsByPoints(Vehicle vehicle, ZonedDateTime begin, ZonedDateTime end) {
        List<Trip> trips = tripRepository.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(
                vehicle,
                begin.withZoneSameInstant(ZoneId.of("UTC")),
                end.withZoneSameInstant(ZoneId.of("UTC")));
        if (trips.isEmpty()) {
            return new ArrayList<>();
        }
        ZonedDateTime minBegin = trips.stream()
                .map(Trip::getBegin)
                .min(ZonedDateTime::compareTo)
                .get();
        ZonedDateTime maxEnd = trips.stream()
                .map(Trip::getEnd)
                .max(ZonedDateTime::compareTo)
                .get();
        List<VehicleLocation> allLocations = vehicleLocationRepository
                .findAllByVehicleAndTimestampBetween(vehicle, minBegin, maxEnd);
        return allLocations.stream()
                .filter(location -> trips.stream()
                        .anyMatch(trip ->
                                !location.getTimestamp().isBefore(trip.getBegin()) &&
                                        !location.getTimestamp().isAfter(trip.getEnd())
                        ))
                .toList();
    }

    public List<TripDto> getTrips(User user, Long id, ZonedDateTime begin, ZonedDateTime end) {
        Vehicle vehicle = findById(user, id);
        ZoneId timeZone = vehicle.getEnterprise().getTimeZone();
        return tripRepository.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(
                vehicle,
                begin.withZoneSameInstant(ZoneId.of("UTC")),
                end.withZoneSameInstant(ZoneId.of("UTC"))
        ).stream()
                .map(trip -> tripMapper.tripToTripDto(trip, timeZone))
                .toList();
    }
}
