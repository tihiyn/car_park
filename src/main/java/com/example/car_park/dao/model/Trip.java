package com.example.car_park.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "trips")
@Getter
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ZonedDateTime begin;
    private ZonedDateTime end;

    // TODO: добавить параметры для аннотаций
    @ManyToOne
    private Vehicle vehicle;
    @OneToOne
    private VehicleLocation beginLocation;
    @OneToOne
    private VehicleLocation endLocation;
}
