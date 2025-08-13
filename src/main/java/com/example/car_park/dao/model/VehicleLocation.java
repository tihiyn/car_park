package com.example.car_park.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.ZonedDateTime;

@Entity
@Table(name = "vehicle_locations")
@Getter
@Setter
public class VehicleLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "GEOGRAPHY(Point, 4326)")
    private Point location;
    private ZonedDateTime timestamp;

    @ManyToOne
    private Vehicle vehicle;
}
