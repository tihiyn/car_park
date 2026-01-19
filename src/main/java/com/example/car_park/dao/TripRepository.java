package com.example.car_park.dao;

import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(Vehicle v, ZonedDateTime b, ZonedDateTime e);
    Page<Trip> findAllByVehicle_Enterprise_IdAndBeginGreaterThanEqualAndEndLessThanEqual(Long eId,
                                                                                         ZonedDateTime b,
                                                                                         ZonedDateTime e,
                                                                                         Pageable p);
    List<Trip> findAllByVehicle(Vehicle v);
}
