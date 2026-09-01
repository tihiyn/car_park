package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.response.ErrorResponse;
import com.example.car_park.controllers.providers.ReportProvider;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.VehicleMileageReport;
import com.example.car_park.enums.Period;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Отчёты", description = "Отчёты по автопарку")
@SecurityRequirement(name = "jwtCookie")
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "Запрос без JWT либо автомобиль принадлежит "
                + "предприятию, недоступному менеджеру",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "302", description = "Пользователь аутентифицирован, но не имеет роли MANAGER: "
                + "отдаётся redirect на /error/forbidden, а не JSON",
                content = @Content)
})
public class ReportRestController {
    private final ReportProvider rp;

    @GetMapping("vehicle/mileage")
    @Operation(
            summary = "Отчёт о пробеге автомобиля",
            description = """
                    Считает пробег автомобиля за интервал с разбивкой по периодам
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Отчёт построен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VehicleMileageReport.class))),
            @ApiResponse(responseCode = "400", description = "Указано неизвестное значение period",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Автомобиль с указанным регистрационным номером не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleMileageReport> getVehicleMileageReport(
            @Parameter(hidden = true) @AuthenticationPrincipal User u,
            @Parameter(description = "Регистрационный номер автомобиля", example = "A123BC")
            @RequestParam("regNum") String regNum,
            @Parameter(description = "Единицы периода",
                    schema = @Schema(type = "string", allowableValues = {"day", "month", "year"}, example = "day"))
            @RequestParam("period") Period p,
            @Parameter(description = "Начало отчётного интервала", example = "2024-01-01T00:00:00Z")
            @RequestParam("begin") ZonedDateTime b,
            @Parameter(description = "Конец отчётного интервала", example = "2024-01-31T23:59:59Z")
            @RequestParam("end") ZonedDateTime e) {
        VehicleMileageReport report = rp.buildVehicleMileageReport(u, regNum, p, b, e);
        return ResponseEntity.ok(report);
    }
}
