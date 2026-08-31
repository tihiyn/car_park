package com.example.car_park.controllers.dto.response;


import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Водитель")
public class DriverDto {

    @Schema(description = "Идентификатор водителя", example = "5")
    private Long id;

    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия", example = "Иванов")
    private String lastName;

    @Schema(description = "Номер водительского удостоверения", example = "5804123456")
    private String driverLicense;

    @Schema(description = "Заработная плата", example = "75000.00")
    private BigDecimal salary;

    @Schema(description = "Номер телефона", example = "+79001234567")
    private String phoneNumber;

    @Schema(description = "Идентификатор автомобиля, за которым водитель закреплён активным. null, если такого нет", example = "1")
    private Long activeVehicleId;

    @ArraySchema(
            schema = @Schema(description = "Идентификатор автомобиля", example = "1"),
            arraySchema = @Schema(description = "Идентификаторы автомобилей, к которым водитель прикреплён")
    )
    private List<Long> vehicleIds;
}
