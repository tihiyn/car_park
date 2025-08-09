package com.example.car_park.service;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.mapper.VehicleMapper;
import com.example.car_park.dao.model.Brand;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

    public VehicleService(VehicleRepository vehicleRepository, VehicleMapper vehicleMapper,
                          ManagerService managerService, BrandService brandService,
                          @Lazy EnterpriseService enterpriseService, DriverService driverService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMapper = vehicleMapper;
        this.managerService = managerService;
        this.brandService = brandService;
        this.enterpriseService = enterpriseService;
        this.driverService = driverService;
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
        System.out.println("Pageable received: page = " + pageable.getPageNumber() +
                ", size = " + pageable.getPageSize() +
                ", sort = " + pageable.getSort());
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
}
