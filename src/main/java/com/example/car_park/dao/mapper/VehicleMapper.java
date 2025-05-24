package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.VehicleDto;
import com.example.car_park.dao.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mappings({
            @Mapping(target = "brandId", source = "vehicle.brand.id")
    })
    VehicleDto vehicleToVehicleDto(Vehicle vehicle);
}
