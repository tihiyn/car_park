package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.*;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.VehicleLocationRepository;
import com.example.car_park.dao.mapper.TripMapper;
import com.example.car_park.dao.mapper.VehicleLocationMapper;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import com.example.car_park.enums.Format;
import io.jenetics.jpx.GPX;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<TripsViewModel> getTripsForUI(User user, Long id, ZonedDateTime begin, ZonedDateTime end) {
        Vehicle vehicle = vehicleService.findById(user, id);
        return getTrips(vehicle, begin, end).stream()
                .map(tripMapper::tripToTripsViewModel)
                .toList();
    }

    public List<TripDto> getTripsForAPI(User user, Long id, ZonedDateTime begin, ZonedDateTime end) {
        Vehicle vehicle = vehicleService.findById(user, id);
        ZoneId timeZone = vehicle.getEnterprise().getTimeZone();
        return getTrips(vehicle, begin, end).stream()
                .map(trip -> tripMapper.tripToTripDto(trip, timeZone))
                .toList();
    }

    public List<Trip> getTrips(Vehicle vehicle, ZonedDateTime begin, ZonedDateTime end) {
        return tripRepository.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(
                        vehicle,
                        begin.withZoneSameInstant(ZoneId.of("UTC")),
                        end.withZoneSameInstant(ZoneId.of("UTC"))
                );
    }

    public ResponseEntity<?> getTripsForMap(List<Long> tripIds) {
        Map<Long, List<VehicleLocation>> tripsMap = new HashMap<>();
        for (Long tripId : tripIds) {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            if (trip != null) {
                List<VehicleLocation> locations = vehicleLocationRepository
                        .findAllByVehicleAndTimestampBetween(
                                trip.getVehicle(),
                                trip.getBegin(),
                                trip.getEnd());
                tripsMap.put(tripId, locations);
            }
        }
        List<GeoJsonFeatureCollection> geoJson = convertToGeoJson(tripsMap);
        return ResponseEntity.ok(geoJson);
    }

    public List<GeoJsonFeatureCollection> convertToGeoJson(Map<Long, List<VehicleLocation>> tripsMap) {
        List<GeoJsonFeatureCollection> result = new ArrayList<>();

        for (Map.Entry<Long, List<VehicleLocation>> entry : tripsMap.entrySet()) {
            Long tripId = entry.getKey();
            List<VehicleLocation> locations = entry.getValue();

            // Преобразуем VehicleLocation в массив координат
            List<List<Double>> coordinates = locations.stream()
                    .map(loc -> List.of(loc.getLocation().getX(), loc.getLocation().getY())) // X=lon, Y=lat
                    .toList();

            GeoJsonFeature feature = new GeoJsonFeature(
                    "Feature",
                    Map.of("tripId", tripId),
                    new GeoJsonGeometry("LineString", coordinates)
            );

            GeoJsonFeatureCollection collection = new GeoJsonFeatureCollection("FeatureCollection", List.of(feature));
            result.add(collection);
        }

        return result;
    }

    public void saveFromFile(User user, Long vehicleId) {
        GPX gpx;
        try {
            gpx = GPX.read(Path.of("track.gpx"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Vehicle vehicle = vehicleService.findById(user, vehicleId);
        ZoneId tz = vehicle.getEnterprise().getTimeZone();
        List<VehicleLocation> locations = gpx.getWayPoints().stream()
            .map(wp -> vehicleLocationMapper.wayPointToVehicleLocation(wp, vehicle, tz))
            .toList();
        if (locations.isEmpty()) {
            return;
        }
        VehicleLocation begin = locations.getFirst();
        VehicleLocation end = locations.getLast();
        checkTripNotIntersectsWithOthers(vehicle, begin, end);
        saveNewTrip(vehicle, locations, begin, end);
    }

    private void checkTripNotIntersectsWithOthers(Vehicle vehicle,
                                                  VehicleLocation begin,
                                                  VehicleLocation end) {
        List<Trip> trips = tripRepository.findAllByVehicle(vehicle);
        if (trips.stream()
            .anyMatch(t -> t.getBegin().isBefore(begin.getTimestamp()) && t.getEnd().isAfter(begin.getTimestamp())
                || t.getBegin().isBefore(end.getTimestamp()) && t.getEnd().isBefore(end.getTimestamp()))) {
            throw new RuntimeException();
        }
    }

    private void saveNewTrip(Vehicle vehicle, List<VehicleLocation> locations,
                             VehicleLocation begin, VehicleLocation end) {
        Trip newTrip = new Trip()
            .setBegin(begin.getTimestamp())
            .setBeginLocation(begin)
            .setEnd(end.getTimestamp())
            .setEndLocation(end)
            .setVehicle(vehicle);
        vehicleLocationRepository.saveAll(locations);
        tripRepository.save(newTrip);
    }
}
