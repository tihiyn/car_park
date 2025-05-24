package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.BrandDto;
import com.example.car_park.dao.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    @Mappings({
            @Mapping(target = "transmission", source = "brand.transmission.value")
    })
    BrandDto brandToBrandDto(Brand brand);
}
