package com.example.car_park.controllers.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Точка трека автомобиля. Поля со значением null в ответ не попадают")
public class VehicleLocationJsonDto {

    @Schema(description = "Широта", example = "53.195873")
    private Double latitude;

    @Schema(description = "Долгота", example = "45.018758")
    private Double longitude;

    @Schema(description = "Момент фиксации точки в часовом поясе предприятия", example = "2024-01-01T08:15:00+03:00")
    private ZonedDateTime timestamp;
}
