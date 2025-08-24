package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.VehicleLocationJsonDto;
import com.example.car_park.dao.model.VehicleLocation;
import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface VehicleLocationMapper {
    @Mappings({
            @Mapping(target = "latitude", expression = "java(location.getLocation().getY())"),
            @Mapping(target = "longitude", expression = "java(location.getLocation().getX())"),
            @Mapping(target = "timestamp", expression = "java(location.getTimestamp().withZoneSameInstant(timeZone))")
    })
    VehicleLocationJsonDto vehicleLocationToVehicleLocationJsonDto(VehicleLocation location, ZoneId timeZone);

    default Map<String, Object> vehicleLocationsToGeoJsonMap(List<VehicleLocation> locations, ZoneId timeZone) {
        List<Map<String, Object>> features = new ArrayList<>();
        for (VehicleLocation loc : locations) {
            Point p = loc.getLocation();
            Map<String, Object> geometry = Map.of(
                    "type", "Point",
                    "coordinates", List.of(p.getX(), p.getY()) // X=долгота, Y=широта
            );
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("name", loc.getTimestamp().withZoneSameInstant(timeZone));
            properties.put("description", "Coordinates");
            Map<String, Object> feature = Map.of(
                    "type", "Feature",
                    "geometry", geometry,
                    "properties", properties
            );
            features.add(feature);
        }
        return Map.of(
                "type", "FeatureCollection",
                "features", features
        );
    }
}
