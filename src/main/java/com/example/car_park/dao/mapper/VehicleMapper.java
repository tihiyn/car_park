package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.VehicleCreateDto;
import com.example.car_park.controllers.dto.response.VehicleEditDto;
import com.example.car_park.controllers.dto.response.VehicleInfoViewModel;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.model.Brand;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

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

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "enterprise", ignore = true)
    @Mapping(target = "drivers", ignore = true)
    @Mapping(target = "activeDriver", ignore = true)
    void vehicleRequestDtoToVehicle(VehicleRequestDto vehicleRequestDto, @MappingTarget Vehicle vehicle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "enterprise", source = "enterprise")
    @Mapping(target = "drivers", source = "drivers")
    @Mapping(target = "activeDriver", source = "activeDriver")
    Vehicle vehicleRequestDtoToVehicle(VehicleRequestDto vehicleRequestDto, Brand brand, Enterprise enterprise,
                                       List<Driver> drivers, Driver activeDriver);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", source = "b")
    @Mapping(target = "enterprise", source = "e")
    @Mapping(target = "drivers", source = "ds")
    @Mapping(target = "activeDriver", source = "ad")
    Vehicle createDtoToEntity(VehicleCreateDto dto, Brand b, Enterprise e, List<Driver> ds, Driver ad);

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "enterprise", ignore = true)
    @Mapping(target = "drivers", ignore = true)
    @Mapping(target = "activeDriver", ignore = true)
    void editDtoToEntity(VehicleEditDto dto, @MappingTarget Vehicle v);

    @Mapping(target = "isAvailable", expression = "java(vehicle.isAvailable() ? \"Да\" : \"Нет\")")
    @Mapping(target = "brand", source = "vehicle.brand.name")
    @Mapping(target = "activeDriver", expression = "java(vehicle.getActiveDriver() == null ? null: String.format(\"%s %s\", vehicle.getActiveDriver().getLastName(), vehicle.getActiveDriver().getFirstName()))")
    @Mapping(target = "drivers", expression = "java(getDriversNames(vehicle.getDrivers()))")
    VehicleInfoViewModel toModel(Vehicle vehicle);

    default String getDriversNames(List<Driver> drivers) {
        return drivers.stream()
                .map(driver -> String.format("%s %s", driver.getLastName(), driver.getFirstName()))
                .collect(Collectors.joining(", "));
    }

    @Mapping(target = "brand", expression = "java(brandToEdit(v.getBrand()))")
    @Mapping(target = "activeDriver", expression = "java(driverToEdit(v.getActiveDriver()))")
    @Mapping(target = "drivers", expression = "java(driversToEdit(v.getDrivers()))")
    VehicleEditDto toEdit(Vehicle v);

    default VehicleEditDto.BrandEditDto brandToEdit(Brand b) {
        if (b == null) {
            return null;
        }
        return new VehicleEditDto.BrandEditDto()
            .setId(b.getId())
            .setName(b.getName());
    }

    default VehicleEditDto.DriverEditDto driverToEdit(Driver d) {
        if (d == null) {
            return null;
        }
        return new VehicleEditDto.DriverEditDto()
            .setId(d.getId())
            .setName("%s %s".formatted(d.getLastName(), d.getFirstName()));
    }

    default List<VehicleEditDto.DriverEditDto> driversToEdit(List<Driver> ds) {
        if (ds == null) {
            return null;
        }
        return ds.stream()
            .map(this::driverToEdit)
            .toList();
    }
}
