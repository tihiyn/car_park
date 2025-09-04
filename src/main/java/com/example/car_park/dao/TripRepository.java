package com.example.car_park.dao;

import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(Vehicle vehicle, ZonedDateTime begin, ZonedDateTime end);
    List<Trip> findAllByBeginGreaterThanEqualAndEndLessThanEqual(ZonedDateTime begin, ZonedDateTime end);
}
