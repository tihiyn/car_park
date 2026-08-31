package com.example.car_park.controllers.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(description = "Поездка автомобиля с адресами начальной и конечной точек")
public class TripDto {

    @Schema(description = "Идентификатор поездки", example = "1")
    private Long id;

    @Schema(description = "Информация о начале поездки")
    private BeginInfo beginInfo;

    @Schema(description = "Информация о завершении поездки")
    private EndInfo endInfo;

    @Data
    @Schema(description = "Начальная точка поездки")
    public static class BeginInfo {

        @Schema(description = "Адрес начальной точки, полученный обратным геокодированием", example = "Россия, Пенза, ул. Московская, 1")
        private String beginAddress;

        @Schema(description = "Широта начальной точки", example = "53.195873")
        private Double beginLat;

        @Schema(description = "Долгота начальной точки", example = "45.018758")
        private Double beginLong;

        @Schema(description = "Время начала поездки в часовом поясе предприятия", example = "2024-01-01T08:00:00+03:00")
        private ZonedDateTime beginTS;
    }

    @Data
    @Schema(description = "Конечная точка поездки")
    public static class EndInfo {

        @Schema(description = "Адрес конечной точки, полученный обратным геокодированием", example = "Россия, Пенза, пр. Строителей, 10")
        private String endAddress;

        @Schema(description = "Широта конечной точки", example = "53.212345")
        private Double endLat;

        @Schema(description = "Долгота конечной точки", example = "45.031122")
        private Double endLong;

        @Schema(description = "Время окончания поездки в часовом поясе предприятия", example = "2024-01-01T08:45:00+03:00")
        private ZonedDateTime endTS;
    }
}
