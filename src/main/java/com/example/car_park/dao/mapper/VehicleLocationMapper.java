package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.VehicleLocationDto;
import com.example.car_park.dao.model.VehicleLocation;
import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface VehicleLocationMapper {
    @Mappings({
            @Mapping(target = "latitude", expression = "java(getLatitude(vehicleLocation, format))"),
            @Mapping(target = "longitude", expression = "java(getLongitude(vehicleLocation, format))"),
            @Mapping(target = "geometry", expression = "java(getGeometry(vehicleLocation, format))"),
            @Mapping(target = "timestamp", expression = "java(vehicleLocation.getTimestamp().withZoneSameInstant(timeZone))")
    })
    VehicleLocationDto vehicleLocationToVehicleLocationDto(VehicleLocation vehicleLocation, ZoneId timeZone, String format);

//    @Named("getLatitude")
    default Double getLatitude(VehicleLocation location, String format) {
        if ("geoJson".equals(format)) {
            return null;
        }

        return location.getLocation().getY();
    }

//    @Named("getLongitude")
    default Double getLongitude(VehicleLocation location, String format) {
        if ("geoJson".equals(format)) {
            return null;
        }

        return location.getLocation().getX();
    }

//    @Named("getGeometry")
    default Point getGeometry(VehicleLocation location, String format) {
        if ("geoJson".equals(format)) {
            return location.getLocation();
        }

        return null;
    }
}
