package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.VehicleLocationRepository;
import com.example.car_park.dao.mapper.TripMapper;
import com.example.car_park.dao.mapper.VehicleLocationMapper;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import com.example.car_park.enums.Format;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final VehicleService vehicleService;
    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final VehicleLocationMapper vehicleLocationMapper;
    private final VehicleLocationRepository vehicleLocationRepository;

    public ResponseEntity<?> getTripsByPointsForAPI(User user, Long id, ZonedDateTime begin, ZonedDateTime end, Format format) {
        Vehicle vehicle = vehicleService.findById(user, id);
        List<VehicleLocation> locations = getTripsByPoints(vehicle, begin, end);
        ZoneId timeZone = vehicle.getEnterprise().getTimeZone();
        if (format == Format.JSON) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(locations.stream()
                            .map(location -> vehicleLocationMapper.vehicleLocationToVehicleLocationJsonDto(location, timeZone))
                            .toList()
                    );
        }
        return ResponseEntity.ok()
                .body(vehicleLocationMapper.vehicleLocationsToGeoJsonMap(locations, timeZone));
    }

    public List<VehicleLocation> getTripsByPoints(Vehicle vehicle, ZonedDateTime begin, ZonedDateTime end) {
        List<Trip> trips = tripRepository.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(
                vehicle,
                begin.withZoneSameInstant(ZoneId.of("UTC")),
                end.withZoneSameInstant(ZoneId.of("UTC")));
        if (trips.isEmpty()) {
            return new ArrayList<>();
        }
        ZonedDateTime minBegin = trips.stream()
                .map(Trip::getBegin)
                .min(ZonedDateTime::compareTo)
                .get();
        ZonedDateTime maxEnd = trips.stream()
                .map(Trip::getEnd)
                .max(ZonedDateTime::compareTo)
                .get();
        List<VehicleLocation> allLocations = vehicleLocationRepository
                .findAllByVehicleAndTimestampBetween(vehicle, minBegin, maxEnd);
        return allLocations.stream()
                .filter(location -> trips.stream()
                        .anyMatch(trip ->
                                !location.getTimestamp().isBefore(trip.getBegin()) &&
                                        !location.getTimestamp().isAfter(trip.getEnd())
                        ))
                .toList();
    }

    public List<TripDto> getTrips(User user, Long id, ZonedDateTime begin, ZonedDateTime end) {
        Vehicle vehicle = vehicleService.findById(user, id);
        ZoneId timeZone = vehicle.getEnterprise().getTimeZone();
        return tripRepository.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(
                        vehicle,
                        begin.withZoneSameInstant(ZoneId.of("UTC")),
                        end.withZoneSameInstant(ZoneId.of("UTC"))
                ).stream()
                .map(trip -> tripMapper.tripToTripDto(trip, timeZone))
                .toList();
    }
}
