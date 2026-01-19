package com.example.car_park.dao.mapper;

import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.controllers.dto.response.TripsViewModel;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.VehicleLocation;
import com.example.car_park.api.AddressClient;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface TripMapper {
    @Mapping(target = "beginInfo.beginLat", expression = "java(trip.getBeginLocation().getLocation().getY())")
    @Mapping(target = "beginInfo.beginLong", expression = "java(trip.getBeginLocation().getLocation().getX())")
    @Mapping(target = "beginInfo.beginTS", expression = "java(trip.getBegin().withZoneSameInstant(timeZone))")
    @Mapping(target = "endInfo.endLat", expression = "java(trip.getEndLocation().getLocation().getY())")
    @Mapping(target = "endInfo.endLong", expression = "java(trip.getEndLocation().getLocation().getX())")
    @Mapping(target = "endInfo.endTS", expression = "java(trip.getEnd().withZoneSameInstant(timeZone))")
    TripDto tripToTripDto(Trip trip, String beginAddress, String endAddress, @Context ZoneId timeZone);

    @Mapping(target = "beginTS", expression = "java(trip.getBegin())")
    @Mapping(target = "endTS", expression = "java(trip.getEnd())")
    TripsViewModel tripToTripsViewModel(Trip trip, String beginAddress, String endAddress);
}
