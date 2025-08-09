package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.model.Brand;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mappings({
            @Mapping(target = "brandId", source = "vehicle.brand.id"),
            @Mapping(target = "enterpriseId", source = "vehicle.enterprise.id"),
            @Mapping(target = "driverIds", source = "vehicle.drivers"),
            @Mapping(target = "activeDriverId", source = "vehicle.activeDriver.id"),
            @Mapping(target = "purchaseDatetime", expression = "java(convertToEnterpriseTimezone(vehicle))")
    })
    VehicleResponseDto vehicleToVehicleResponseDto(Vehicle vehicle);

    default List<Long> mapDriversToDriverIds(List<Driver> drivers) {
        return drivers.stream()
                .map(Driver::getId)
                .collect(Collectors.toList());
    }

    default ZonedDateTime convertToEnterpriseTimezone(Vehicle vehicle) {
        if (vehicle.getPurchaseDatetime() == null) {
            return null;
        }
        return vehicle.getPurchaseDatetime()
                .withZoneSameInstant(vehicle.getEnterprise().getTimeZone());
    }

    @Mappings({
            @Mapping(target = "brand", ignore = true),
            @Mapping(target = "enterprise", ignore = true),
            @Mapping(target = "drivers", ignore = true),
            @Mapping(target = "activeDriver", ignore = true)
    })
    void vehicleRequestDtoToVehicle(VehicleRequestDto vehicleRequestDto, @MappingTarget Vehicle vehicle);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "brand", source = "brand"),
            @Mapping(target = "enterprise", source = "enterprise"),
            @Mapping(target = "drivers", source = "drivers"),
            @Mapping(target = "activeDriver", source = "activeDriver")
    })
    Vehicle vehicleRequestDtoToVehicle(VehicleRequestDto vehicleRequestDto, Brand brand, Enterprise enterprise,
                                       List<Driver> drivers, Driver activeDriver);
}
