package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.EnterpriseDto;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EnterpriseMapper {
    @Mappings({
            @Mapping(target = "driverIds", source = "enterprise.drivers"),
            @Mapping(target = "vehicleIds", source = "enterprise.vehicles")
    })
    EnterpriseDto enterpriseToEnterpriseDto(Enterprise enterprise);

    default List<Long> mapDriversToId(List<Driver> drivers) {
        return drivers.stream().map(Driver::getId).collect(Collectors.toList());
    }
    default List<Long> mapVehiclesToId(List<Vehicle> vehicles) {
        return vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
    }
}
