package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.request.EnterpriseRequestDto;
import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EnterpriseMapper {
    @Mappings({
            @Mapping(target = "driverIds", source = "enterprise.drivers"),
            @Mapping(target = "vehicleIds", source = "enterprise.vehicles")
    })
    EnterpriseResponseDto enterpriseToEnterpriseResponseDto(Enterprise enterprise);

    default List<Long> mapDriversToId(List<Driver> drivers) {
        return drivers.stream().map(Driver::getId).collect(Collectors.toList());
    }
    default List<Long> mapVehiclesToId(List<Vehicle> vehicles) {
        return vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
    }

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "vehicles", source = "vehicles"),
            @Mapping(target = "drivers", source = "drivers"),
            @Mapping(target = "managers", source = "managers")
    })
    Enterprise enterpriseRequestDtoToEnterprise(EnterpriseRequestDto enterpriseRequestDto, List<Vehicle> vehicles,
                                                List<Driver> drivers, List<Manager> managers);

    @Mappings({
            @Mapping(target = "vehicles", ignore = true),
            @Mapping(target = "drivers", ignore = true),
            @Mapping(target = "managers", ignore = true)
    })
    void enterpriseRequestDtoToEnterprise(EnterpriseRequestDto enterpriseRequestDto, @MappingTarget Enterprise enterprise);
}
