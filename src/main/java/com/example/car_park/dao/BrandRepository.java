package com.example.car_park.dao;

import com.example.car_park.dao.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query(value = "SELECT b FROM Brand b WHERE cast(b.id as string) = :keyword"
            + " OR b.name LIKE '%' || :keyword || '%'"
            + " OR b.type LIKE '%' || :keyword || '%'"
            + " OR cast(b.transmission as string ) LIKE '%' || :keyword || '%'"
            + " OR cast(b.engineVolume as string) = :keyword"
            + " OR cast(b.enginePower as string) = :keyword"
            + " OR cast(b.numOfSeats as string) = :keyword")
    List<Brand> findByKeyword(@Param("keyword") String keyword);
}
