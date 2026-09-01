package com.example.car_park.controllers.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Данные предприятия для создания или редактирования")
public class EnterpriseRequestDto {
    @NotBlank(message = "Название предприятия обязательго")
    @Schema(description = "Название предприятия", example = "Автопарк №1")
    private String name;

    @NotBlank(message = "Название города обязательно")
    @Schema(description = "Город, в котором находится предприятие", example = "Пенза")
    private String city;

    @NotNull(message = "Регистрационный номер обязателен")
    @Size(min = 10, max = 10, message = "Неверный формат регистрационного номера")
    @Schema(description = "Регистрационный номер предприятия, ровно 10 символов", example = "1234567890")
    private String registrationNumber;

    @NotNull(message = "Список транспортных средств не может быть null")
    @ArraySchema(
            schema = @Schema(description = "Идентификатор автомобиля", example = "1"),
            arraySchema = @Schema(description = "Идентификаторы автомобилей предприятия. Может быть пустым, но не null")
    )
    private Set<Long> vehicleIds;

    @NotNull(message = "Список водителей не может быть null")
    @ArraySchema(
            schema = @Schema(description = "Идентификатор водителя", example = "5"),
            arraySchema = @Schema(description = "Идентификаторы водителей предприятия. Может быть пустым, но не null")
    )
    private Set<Long> driverIds;
    // TODO: убедиться, что менеджеры (скрипач) не нужны
//    private Set<Long> managerIds;
}
