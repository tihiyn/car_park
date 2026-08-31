package com.example.car_park.controllers.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Результат импорта данных")
public class ImportResp {

    @Schema(description = "Успешно ли завершился импорт. В JSON поле называется successful", example = "true")
    private boolean isSuccessful;

    @Schema(description = "Текстовое описание результата импорта", example = "Импорт завершён успешно")
    private String desc;
}
