package com.example.car_park.dao.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Отчёт о пробеге автомобиля, разбитый по периодам")
public class VehicleMileageReport extends Report<Map<String, Long>> {

    @Schema(description = "Идентификатор автомобиля, по которому построен отчёт", example = "1")
    private Long vehicleId;

    @Override
    @Schema(
            description = "Пробег в метрах по каждому периоду. Ключ — начало периода",
            example = "{\"2024-01-01\": 120000, \"2024-01-02\": 87000}"
    )
    public Map<String, Long> getResult() {
        return super.getResult();
    }
}
