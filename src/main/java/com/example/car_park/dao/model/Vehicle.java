package com.example.car_park.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinTable;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String regNum;
    private Integer price;
    private Integer mileage;
    private Integer productionYear;
    private String color;
    private boolean isAvailable;
    private ZonedDateTime purchaseDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", referencedColumnName = "id", nullable = false)
    private Brand brand;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", referencedColumnName = "id", nullable = false)
    private Enterprise enterprise;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "active_driver_id", referencedColumnName = "id")
    private Driver activeDriver;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "vehicle_driver_assignments",
            joinColumns = {@JoinColumn(name = "vehicle_id")},
            inverseJoinColumns = {@JoinColumn(name = "driver_id")}
    )
    private List<Driver> drivers = new ArrayList<>();
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> trips = new ArrayList<>();
}
