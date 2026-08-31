package com.example.car_park.dao.model;

import com.example.car_park.enums.Period;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Schema(description = "Базовые поля отчёта")
public class Report<T> {

    @Schema(description = "Название отчёта", example = "vehicleMileage")
    private String name;

    @Schema(description = "Шаг агрегации. В ответе сериализуется в верхнем регистре, в запросе принимается в нижнем", example = "DAY")
    private Period period;

    @Schema(description = "Начало отчётного интервала", example = "2024-01-01T00:00:00+03:00")
    private ZonedDateTime begin;

    @Schema(description = "Конец отчётного интервала", example = "2024-01-31T23:59:59+03:00")
    private ZonedDateTime end;

    @Schema(description = "Результат отчёта: значение по каждому периоду агрегации")
    private T result;
}
