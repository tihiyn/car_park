package com.example.car_park.dao;

import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class VehicleLocationCachedRepository {
    private final VehicleLocationRepository r;

    @Cacheable(value = "locations", key = "T(java.util.Objects).hash(#v.id, #s, #b)", unless = "#result == null")
    public List<VehicleLocation> findAllBetween(Vehicle v, ZonedDateTime s, ZonedDateTime b) {
        return r.findAllByVehicleAndTimestampBetween(v, s, b);
    }
}
