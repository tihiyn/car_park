package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.BrandDto;
import com.example.car_park.controllers.dto.response.VehicleEditDto;
import com.example.car_park.dao.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    @Mapping(target = "transmission", source = "b.transmission.value")
    BrandDto brandToBrandDto(Brand b);


    VehicleEditDto.BrandEditDto toEditDto(Brand b);
}
