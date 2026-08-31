package com.example.car_park.controllers.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Schema(description = "Данные автомобиля для создания или редактирования")
public class VehicleRequestDto {
    @NotBlank(message = "Регистрационный номер обязателен")
    @Size(min = 6, max = 6, message = "Неверный формат регистрационного номера")
    @Schema(description = "Регистрационный номер, ровно 6 символов", example = "A123BC")
    private String regNum;

    @NotNull(message = "Стоимость обязательна")
    @Schema(description = "Стоимость автомобиля", example = "1500000")
    private Integer price;

    @NotNull(message = "Пробег обязателен")
    @Schema(description = "Пробег в километрах", example = "48000")
    private Integer mileage;

    @NotNull(message = "Год производства обязателен")
    @Min(value = 1900, message = "Год производства не может быть раньше 1900")
    @Max(value = 2025, message = "Год производства не может быть позже текущего")
    @Schema(description = "Год производства", example = "2021")
    private Integer productionYear;

    @NotBlank(message = "Цвет обязателен")
    @Schema(description = "Цвет кузова", example = "красный")
    private String color;

    @Schema(description = "Доступен ли автомобиль. В JSON поле называется available", example = "true")
    private boolean isAvailable;

    @NotNull(message = "Транспортное средство должно иметь бренд")
    @Schema(description = "Идентификатор бренда", example = "1")
    private Long brandId;

    @NotNull(message = "Транспортное средство должно принадлежать одному из предприятий")
    @Schema(description = "Идентификатор предприятия, которому принадлежит автомобиль", example = "1")
    private Long enterpriseId;

    @Schema(description = "Идентификатор активного водителя. null, если водитель не назначен", example = "5")
    private Long activeDriverId;

    @NotNull(message = "Список водителей не может быть null")
    @ArraySchema(
            schema = @Schema(description = "Идентификатор водителя", example = "5"),
            arraySchema = @Schema(description = "Идентификаторы водителей, закреплённых за автомобилем. Может быть пустым, но не null")
    )
    private Set<Long> driverIds = new HashSet<>();
}
