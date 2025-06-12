package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.ManagerDto;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ManagerMapper {
    @Mappings({
            @Mapping(target = "enterpriseIds", source = "manager.managedEnterprises", qualifiedByName = "mapEnterprisesToId"),
            @Mapping(target = "vehicleIds", source = "manager.managedEnterprises", qualifiedByName = "mapEnterprisesToVehicleId"),
            @Mapping(target = "driverIds", source = "manager.managedEnterprises", qualifiedByName = "mapEnterprisesToDriverId")
    })
    ManagerDto managerToManagerDto(Manager manager);

    @Named("mapEnterprisesToId")
    default List<Long> mapEnterprisesToId(List<Enterprise> enterprises) {
        return enterprises.stream()
                .map(Enterprise::getId)
                .toList();
    }

    @Named("mapEnterprisesToVehicleId")
    default List<Long> mapEnterprisesToVehicleId(List<Enterprise> enterprises) {
        return enterprises.stream()
                .flatMap(enterprise -> enterprise.getVehicles().stream().map(Vehicle::getId))
                .toList();
    }

    @Named("mapEnterprisesToDriverId")
    default List<Long> mapEnterprisesToDriverId(List<Enterprise> enterprises) {
        return enterprises.stream()
                .flatMap(enterprise -> enterprise.getDrivers().stream().map(Driver::getId))
                .toList();
    }
}
