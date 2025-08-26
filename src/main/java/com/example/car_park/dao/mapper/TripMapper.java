package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.VehicleLocation;
import com.example.car_park.api.AddressClient;
import org.mapstruct.Context;
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
            @Mapping(target = "beginInfo.beginAddress", expression = "java(getAddress(trip.getBeginLocation()))"),
            @Mapping(target = "beginInfo.beginLat", expression = "java(trip.getBeginLocation().getLocation().getY())"),
            @Mapping(target = "beginInfo.beginLong", expression = "java(trip.getBeginLocation().getLocation().getX())"),
            @Mapping(target = "beginInfo.beginTS", expression = "java(trip.getBegin().withZoneSameInstant(timeZone))"),
            @Mapping(target = "endInfo.endAddress", expression = "java(getAddress(trip.getEndLocation()))"),
            @Mapping(target = "endInfo.endLat", expression = "java(trip.getEndLocation().getLocation().getY())"),
            @Mapping(target = "endInfo.endLong", expression = "java(trip.getEndLocation().getLocation().getX())"),
            @Mapping(target = "endInfo.endTS", expression = "java(trip.getEnd().withZoneSameInstant(timeZone))")
    })
    public abstract TripDto tripToTripDto(Trip trip, @Context ZoneId timeZone);

    protected String getAddress(VehicleLocation location) {
        return addressClient.getAddressByCoords(
                location.getLocation().getX(),
                location.getLocation().getY()
        );
    }
}
