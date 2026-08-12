package com.example.car_park.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.BatchSize;
import org.locationtech.jts.geom.Point;

import java.time.ZonedDateTime;

@Entity
@Table(name = "vehicle_locations")
@Getter
@Setter
@Accessors(chain = true)
@BatchSize(size = 10)
public class VehicleLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Point location;
    private ZonedDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vehicle vehicle;
}
