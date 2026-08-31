package com.example.car_park.controllers.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Единый формат ошибки")
public class ErrorResponse {

    @Schema(description = "Момент возникновения ошибки", example = "2024-01-01T12:00:00Z")
    private String timestamp;

    @Schema(description = "HTTP-статус", example = "404")
    private int status;

    @Schema(description = "Название HTTP-статуса", example = "Not Found")
    private String error;

    @Schema(description = "Причина ошибки", example = "Автомобиль не найден")
    private String message;

    @Schema(description = "Путь запроса, вызвавшего ошибку", example = "/api/vehicles/42")
    private String path;
}
