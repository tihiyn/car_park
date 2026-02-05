package com.example.car_park.controllers.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class VehicleCreateDto extends VehicleEditDto {
    private Long enterpriseId;
}
