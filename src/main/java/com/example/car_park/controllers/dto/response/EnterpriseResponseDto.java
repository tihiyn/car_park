package com.example.car_park.controllers.dto.response;


import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Предприятие")
public class EnterpriseResponseDto {

    @Schema(description = "Идентификатор предприятия", example = "1")
    private Long id;

    @Schema(description = "Название предприятия", example = "Автопарк №1")
    private String name;

    @Schema(description = "Город, в котором находится предприятие", example = "Пенза")
    private String city;

    @Schema(description = "Регистрационный номер предприятия", example = "1234567890")
    private String registrationNumber;

    @ArraySchema(
            schema = @Schema(description = "Идентификатор водителя", example = "5"),
            arraySchema = @Schema(description = "Идентификаторы водителей предприятия")
    )
    private List<Long> driverIds;

    @ArraySchema(
            schema = @Schema(description = "Идентификатор автомобиля", example = "1"),
            arraySchema = @Schema(description = "Идентификаторы автомобилей предприятия")
    )
    private List<Long> vehicleIds;
}
