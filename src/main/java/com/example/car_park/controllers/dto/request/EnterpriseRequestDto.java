package com.example.car_park.controllers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class EnterpriseRequestDto {
    @NotBlank(message = "Название предприятия обязательго")
    private String name;
    @NotBlank(message = "Название города обязательно")
    private String city;
    @NotNull(message = "Регистрационный номер обязателен")
    @Size(min = 10, max = 10, message = "Неверный формат регистрационного номера")
    private String registrationNumber;

    @NotNull(message = "Список транспортных средств не может быть null")
    private Set<Long> vehicleIds;
    @NotNull(message = "Список водителей не может быть null")
    private Set<Long> driverIds;
    // TODO: убедиться, что менеджеры (скрипач) не нужны
//    private Set<Long> managerIds;
}
