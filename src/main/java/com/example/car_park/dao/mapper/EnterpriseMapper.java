package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.request.EnterpriseRequestDto;
import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.controllers.dto.response.EnterpriseViewModel;
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
    @Mapping(target = "driverIds", source = "enterprise.drivers")
    @Mapping(target = "vehicleIds", source = "enterprise.vehicles")
    EnterpriseResponseDto enterpriseToEnterpriseResponseDto(Enterprise enterprise);

    default List<Long> mapDriversToId(List<Driver> drivers) {
        return drivers.stream().map(Driver::getId).collect(Collectors.toList());
    }
    default List<Long> mapVehiclesToId(List<Vehicle> vehicles) {
        return vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", source = "vehicles")
    @Mapping(target = "drivers", source = "drivers")
    @Mapping(target = "managers", source = "managers")
    Enterprise enterpriseRequestDtoToEnterprise(EnterpriseRequestDto enterpriseRequestDto, List<Vehicle> vehicles,
                                                List<Driver> drivers, List<Manager> managers);

    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "drivers", ignore = true)
    @Mapping(target = "managers", ignore = true)
    void enterpriseRequestDtoToEnterprise(EnterpriseRequestDto enterpriseRequestDto, @MappingTarget Enterprise enterprise);

    @Mapping(target = "timeZone", expression = "java(e.getTimeZone().toString())")
    @Mapping(target = "drivers", expression = "java(driversToModel(e.getDrivers()))")
    @Mapping(target = "vehicles", expression = "java(vehiclesToModel(e.getVehicles()))")
    EnterpriseViewModel toModel(Enterprise e);

    default List<EnterpriseViewModel.VehicleViewModel> vehiclesToModel(List<Vehicle> vs) {
        return vs.stream()
            .map(v -> new EnterpriseViewModel.VehicleViewModel()
                .setId(v.getId())
                .setRegNum(v.getRegNum())
            ).toList();
    }

    default List<EnterpriseViewModel.DriverInfoViewModel> driversToModel(List<Driver> ds) {
        return ds.stream()
            .map(d -> new EnterpriseViewModel.DriverInfoViewModel()
                .setId(d.getId())
                .setName("%s %s".formatted(d.getLastName(), d.getFirstName()))
            ).toList();
    }
}
