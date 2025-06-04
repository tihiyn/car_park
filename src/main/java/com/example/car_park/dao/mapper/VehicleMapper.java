package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.VehicleDto;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mappings({
            @Mapping(target = "driverIds", source = "vehicle.drivers"),
            @Mapping(target = "activeDriverId", source = "vehicle.activeDriver")
    })
    VehicleDto vehicleToVehicleDto(Vehicle vehicle);

    default List<Long> mapDriversToId(List<Driver> drivers) {
        return drivers.stream()
                .map(Driver::getId)
                .collect(Collectors.toList());
    }

    default Long mapDriverToId(Driver driver) {
        if (driver == null) {
            return -1L;
        }

        return driver.getId();
    }
}
