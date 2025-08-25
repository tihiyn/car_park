package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.VehicleLocation;
import com.example.car_park.service.AddressClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;

@Mapper(
        componentModel = "spring",
        uses = {AddressClient.class}
)
public abstract class TripMapper {
    @Autowired
    protected AddressClient addressClient;

    @Mappings({
            @Mapping(target = "beginAddress", expression = "java(getAddress(trip.getBeginLocation()))"),
            @Mapping(target = "beginLat", expression = "java(trip.getBeginLocation().getLocation().getY())"),
            @Mapping(target = "beginLong", expression = "java(trip.getBeginLocation().getLocation().getX())"),
            @Mapping(target = "beginTS", expression = "java(trip.getBegin().withZoneSameInstant(timeZone))"),
            @Mapping(target = "endAddress", expression = "java(getAddress(trip.getEndLocation()))"),
            @Mapping(target = "endLat", expression = "java(trip.getEndLocation().getLocation().getY())"),
            @Mapping(target = "endLong", expression = "java(trip.getEndLocation().getLocation().getX())"),
            @Mapping(target = "endTS", expression = "java(trip.getEnd().withZoneSameInstant(timeZone))")
    })
    public abstract TripDto tripToTripDto(Trip trip, ZoneId timeZone);

    protected String getAddress(VehicleLocation location) {
        return addressClient.getAddressByCoords(
                location.getLocation().getX(), // Долгота
                location.getLocation().getY()  // Широта
        );
    }
}
