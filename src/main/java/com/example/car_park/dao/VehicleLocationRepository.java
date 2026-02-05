package com.example.car_park.dao;

import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {

    List<VehicleLocation> findVehicleLocationsByVehicleAndTimestampBetween(Vehicle v, ZonedDateTime b, ZonedDateTime e);
    List<VehicleLocation> findAllByVehicleAndTimestampBetween(Vehicle v, ZonedDateTime b, ZonedDateTime e);
}
