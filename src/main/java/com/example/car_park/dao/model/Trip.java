package com.example.car_park.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Entity
@Table(name = "trips")
@Getter
@Setter
@Accessors(chain = true)
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ZonedDateTime begin;
    @Column(name = "\"end\"")
    private ZonedDateTime end;
    private Long length;

    // TODO: добавить параметры для аннотаций
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    @OneToOne(cascade = CascadeType.ALL)
    private VehicleLocation beginLocation;
    @OneToOne(cascade = CascadeType.ALL)
    private VehicleLocation endLocation;
}
