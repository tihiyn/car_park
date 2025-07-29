package com.example.car_park.controllers.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class VehicleRequestDto {
    @NotBlank(message = "Регистрационный номер обязателен")
    @Size(min = 6, max = 6, message = "Неверный формат регистрационного номера")
    private String regNum;
    @NotNull(message = "Стоимость обязательна")
    private Integer price;
    @NotNull(message = "Пробег обязателен")
    private Integer mileage;
    @NotNull(message = "Год производства обязателен")
    @Min(value = 1900, message = "Год производства не может быть раньше 1900")
    @Max(value = 2025, message = "Год производства не может быть позже текущего")
    private Integer productionYear;
    @NotBlank(message = "Цвет обязателен")
    private String color;
    private boolean isAvailable;

    @NotNull(message = "Транспортное средство должно иметь бренд")
    private Long brandId;
    @NotNull(message = "Транспортное средство должно принадлежать одному из предприятий")
    private Long enterpriseId;
    private Long activeDriverId;

    @NotNull(message = "Список водителей не можеть быть null")
    private Set<Long> driverIds = new HashSet<>();
}
