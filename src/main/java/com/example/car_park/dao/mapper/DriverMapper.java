package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mappings({
            @Mapping(target = "vehicleIds", source = "driver.vehicles"),
            @Mapping(target = "activeVehicleId", source = "driver.activeVehicle")
    })
    DriverDto driverToDriverDto(Driver driver);

    default List<Long> mapVehiclesToLong(List<Vehicle> vehicles) {
        return vehicles.stream()
                .map(Vehicle::getId)
                .collect(Collectors.toList());
    }

    default Long mapVehicleToLong(Vehicle vehicle) {
        if (vehicle == null) {
            return -1L;
        }

        return vehicle.getId();
    }
}
