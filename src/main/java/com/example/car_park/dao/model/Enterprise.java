package com.example.car_park.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@Entity
@Table(name = "enterprises")
@Getter
@Setter
public class Enterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String city;
    @Column(unique = true)
    private String registrationNumber;
    @OneToMany(mappedBy = "enterprise",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private List<Vehicle> vehicles;
    @OneToMany(mappedBy = "enterprise",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private List<Driver> drivers;
    @ManyToMany(mappedBy = "managedEnterprises")
    private List<Manager> managers;

    @PreRemove
    private void preventDeleteIfHasVehiclesOrDrivers() {
        if (!vehicles.isEmpty() || !drivers.isEmpty()) {
            throw new ResponseStatusException(CONFLICT,
                    String.format("Нельзя удалить предприятие: в нём есть транспортные средства %s и/или водители %s!",
                            vehicles.stream().map(Vehicle::getId).toList(),
                            drivers.stream().map(Driver::getId).toList())
            );
        }
    }
}
