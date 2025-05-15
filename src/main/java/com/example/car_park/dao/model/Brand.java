package com.example.car_park.dao.model;

import com.example.car_park.enums.Transmission;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    @Enumerated(value = EnumType.STRING)
    private Transmission transmission;
    private Double engineVolume;
    private Integer enginePower;
    private Integer numOfSeats;
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private List<Vehicle> vehicles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brand brand = (Brand) o;
        return Objects.equals(name, brand.name) && Objects.equals(type, brand.type) && transmission == brand.transmission && Objects.equals(engineVolume, brand.engineVolume) && Objects.equals(enginePower, brand.enginePower) && Objects.equals(numOfSeats, brand.numOfSeats) && Objects.equals(vehicles, brand.vehicles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, transmission, engineVolume, enginePower, numOfSeats, vehicles);
    }
}
