package com.example.car_park.service;

import com.example.car_park.controllers.dto.request.EnterpriseRequestDto;
import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.dao.EnterpriseRepository;
import com.example.car_park.dao.mapper.EnterpriseMapper;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
public class EnterpriseService {
    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseMapper enterpriseMapper;
    private final VehicleService vehicleService;
    private final ManagerService managerService;
    private final DriverService driverService;

    public EnterpriseService(EnterpriseRepository enterpriseRepository, EnterpriseMapper enterpriseMapper,
                             @Lazy VehicleService vehicleService, ManagerService managerService,
                             DriverService driverService) {
        this.enterpriseRepository = enterpriseRepository;
        this.enterpriseMapper = enterpriseMapper;
        this.vehicleService = vehicleService;
        this.managerService = managerService;
        this.driverService = driverService;
    }

    public Enterprise findById(User user, Long id) {
        Enterprise enterprise = enterpriseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Предприятие с id=%d отсутствует", id)));

        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .filter(e -> e.getId().equals(enterprise.getId()))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN,
                        String.format("Вы не управляете предприятием с id=%d", id)));
    }

    public EnterpriseResponseDto findByIdForRest(User user, Long id) {
        Enterprise enterprise = enterpriseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Предприятие с id=%d отсутствует", id)));

        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .filter(e -> e.getId().equals(enterprise.getId()))
                .map(enterpriseMapper::enterpriseToEnterpriseResponseDto)
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN,
                        String.format("Вы не управляете предприятием с id=%d", id)));
    }

    public List<EnterpriseResponseDto> findAllForRest(User user) {
        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .map(enterpriseMapper::enterpriseToEnterpriseResponseDto)
                .toList();
    }

    public Enterprise create(User user, EnterpriseRequestDto enterpriseRequestDto) {
        List<Vehicle> vehicles = vehicleService.findAllByIds(user, enterpriseRequestDto.getVehicleIds());
        List<Driver> drivers = driverService.findAllByIds(user, enterpriseRequestDto.getDriverIds());
        List<Manager> managers = new ArrayList<>(List.of(managerService.getManagerByUser(user)));

        Enterprise enterprise = enterpriseMapper.enterpriseRequestDtoToEnterprise(enterpriseRequestDto, vehicles, drivers, managers);
        vehicles.forEach(vehicle -> vehicle.setEnterprise(enterprise));
        drivers.forEach(driver -> driver.setEnterprise(enterprise));
        managers.forEach(manager -> manager.getManagedEnterprises().add(enterprise));

        return enterpriseRepository.save(enterprise);
    }

    public EnterpriseResponseDto update(User user, Long id, EnterpriseRequestDto enterpriseRequestDto) {
        Enterprise oldEnterprise = findById(user, id);
        enterpriseMapper.enterpriseRequestDtoToEnterprise(enterpriseRequestDto, oldEnterprise);

        if (!oldEnterprise.getVehicles().stream()
                .map(Vehicle::getId)
                .collect(Collectors.toSet()).equals(enterpriseRequestDto.getVehicleIds())) {
            List<Vehicle> vehicles = vehicleService.findAllByIds(user, enterpriseRequestDto.getVehicleIds());
            oldEnterprise.getVehicles().clear();
            oldEnterprise.getVehicles().addAll(vehicles);
        }

        if (!oldEnterprise.getDrivers().stream()
                .map(Driver::getId)
                .collect(Collectors.toSet()).equals(enterpriseRequestDto.getDriverIds())) {
            List<Driver> drivers = driverService.findAllByIds(user, enterpriseRequestDto.getDriverIds());
            oldEnterprise.getDrivers().clear();
            oldEnterprise.getDrivers().addAll(drivers);
        }

        return enterpriseMapper.enterpriseToEnterpriseResponseDto(enterpriseRepository.save(oldEnterprise));
    }

    public void delete(User user, Long id) {
        Enterprise enterpriseToDelete = findById(user, id);

        managerService.getManagerByUser(user).getManagedEnterprises().remove(enterpriseToDelete);
        enterpriseRepository.delete(enterpriseToDelete);
    }
}
