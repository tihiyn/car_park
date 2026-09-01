package com.example.car_park.controllers.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Schema(description = "Автомобиль")
public class VehicleResponseDto {

    @Schema(description = "Идентификатор автомобиля", example = "1")
    private Long id;

    @Schema(description = "Регистрационный номер", example = "A123BC")
    private String regNum;

    @Schema(description = "Стоимость автомобиля", example = "1500000")
    private Integer price;

    @Schema(description = "Пробег в километрах", example = "48000")
    private Integer mileage;

    @Schema(description = "Год производства", example = "2021")
    private Integer productionYear;

    @Schema(description = "Цвет кузова", example = "красный")
    private String color;

    @Schema(description = "Доступен ли автомобиль. В JSON поле называется available", example = "true")
    private boolean isAvailable;

    @Schema(description = "Дата и время покупки в часовом поясе предприятия", example = "2023-04-15T10:30:00+03:00")
    private ZonedDateTime purchaseDatetime;

    @Schema(description = "Идентификатор бренда", example = "1")
    private Long brandId;

    @Schema(description = "Идентификатор предприятия", example = "1")
    private Long enterpriseId;

    @Schema(description = "Идентификатор активного водителя. null, если водитель не назначен", example = "5")
    private Long activeDriverId;

    @ArraySchema(
            schema = @Schema(description = "Идентификатор водителя", example = "5"),
            arraySchema = @Schema(description = "Идентификаторы водителей, закреплённых за автомобилем")
    )
    private List<Long> driverIds;
}
