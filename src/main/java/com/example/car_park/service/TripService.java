package com.example.car_park.service;

import com.example.car_park.controllers.dto.VehicleState;
import com.example.car_park.controllers.dto.response.GeoJsonFeature;
import com.example.car_park.controllers.dto.response.GeoJsonFeatureCollection;
import com.example.car_park.controllers.dto.response.GeoJsonGeometry;
import com.example.car_park.controllers.dto.response.VehicleLocationJsonDto;
import com.example.car_park.dao.mapper.VehicleLocationMapper;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import io.jenetics.jpx.GPX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TripService {
    private final VehicleLocationMapper vehicleLocationMapper;

    public ZonedDateTime findMinBegin(List<Trip> ts) {
        return ts.stream()
            .map(Trip::getBegin)
            .min(ZonedDateTime::compareTo)
            .get();
    }

    public ZonedDateTime findMaxEnd(List<Trip> ts) {
        return ts.stream()
            .map(Trip::getEnd)
            .max(ZonedDateTime::compareTo)
            .get();
    }

    public List<VehicleLocation> filterByTripsBounds(List<VehicleLocation> locs, List<Trip> trips) {
        return locs.stream()
            .filter(location -> trips.stream()
                .anyMatch(trip ->
                    !location.getTimestamp().isBefore(trip.getBegin()) &&
                        !location.getTimestamp().isAfter(trip.getEnd())
                ))
            .toList();
    }

    public List<GeoJsonFeatureCollection> convertToGeoJson(Map<Long, List<VehicleLocation>> tripsMap) {
        List<GeoJsonFeatureCollection> result = new ArrayList<>();
        for (Map.Entry<Long, List<VehicleLocation>> entry : tripsMap.entrySet()) {
            Long tripId = entry.getKey();
            List<VehicleLocation> locations = entry.getValue();
            List<List<Double>> coordinates = locations.stream()
                .map(loc -> List.of(loc.getLocation().getX(), loc.getLocation().getY()))
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

    public List<VehicleLocation> getLocationsFomGPX(GPX gpx, Vehicle v, List<Trip> saved) {
        ZoneId tz = v.getEnterprise().getTimeZone();
        List<VehicleLocation> locations = gpx.getTracks().get(0).getSegments().get(0).getPoints().stream()
            .map(wp -> vehicleLocationMapper.wayPointToVehicleLocation(wp, v, tz))
            .toList();
        if (locations.isEmpty()) {
            throw new RuntimeException("В файле отсутствуют поездки");
        }
        VehicleLocation begin = locations.getFirst();
        VehicleLocation end = locations.getLast();
        if (!checkTripNotIntersectsWithOthers(saved, begin, end)) {
            throw new RuntimeException("Поездка пересекается с существующими");
        }
        return locations;
    }

    private boolean checkTripNotIntersectsWithOthers(List<Trip> saved,
                                                  VehicleLocation begin,
                                                  VehicleLocation end) {
        return saved.stream()
            .noneMatch(t -> t.getBegin().isBefore(begin.getTimestamp()) && t.getEnd().isAfter(begin.getTimestamp())
                || t.getBegin().isBefore(end.getTimestamp()) && t.getEnd().isAfter(end.getTimestamp()));
    }

    public VehicleLocationJsonDto getNewPoint(VehicleState s) {
        if (s.getBearing() < 0) s.setBearing(s.getBearing() + 360);
        if (s.getBearing() >= 360) s.setBearing(s.getBearing()- 360);
        double distance = s.getSpeed() / 3.6;
        double R = 6371000;
        double latRad = Math.toRadians(s.getLat());
        double lonRad = Math.toRadians(s.getLon());
        double bearingRad = Math.toRadians(s.getBearing());
        double newLat = Math.asin(
            Math.sin(latRad) * Math.cos(distance / R) +
                Math.cos(latRad) * Math.sin(distance / R) * Math.cos(bearingRad)
        );
        double newLon = lonRad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(distance / R) * Math.cos(latRad),
            Math.cos(distance / R) - Math.sin(latRad) * Math.sin(newLat)
        );
        s.setLat(Math.toDegrees(newLat));
        s.setLon(Math.toDegrees(newLon));
        return new VehicleLocationJsonDto()
            .setLatitude(s.getLat())
            .setLongitude(s.getLon())
            .setTimestamp(ZonedDateTime.now());
    }
}
