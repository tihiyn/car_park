package com.example.car_park.dao;

import com.example.car_park.dao.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    @Query(value = "SELECT v FROM Vehicle v WHERE cast(v.id as string) = :keyword"
            + " OR v.regNum LIKE '%' || :keyword || '%'"
            + " OR cast(v.price as string) = :keyword"
            + " OR cast(v.mileage as string) = :keyword"
            + " OR cast(v.productionYear as string) = :keyword"
            + " OR v.color LIKE '%' || :keyword || '%'"
            + " OR v.brand.name LIKE '%' || :keyword || '%'"
            + " OR v.brand.type LIKE '%' || :keyword || '%'"
            + " OR cast(v.brand.transmission as string ) LIKE '%' || :keyword || '%'"
            + " OR cast(v.brand.engineVolume as string) = :keyword"
            + " OR cast(v.brand.enginePower as string) = :keyword"
            + " OR cast(v.brand.numOfSeats as string) = :keyword")
    List<Vehicle> findByKeyword(@Param("keyword") String keyword);
}
