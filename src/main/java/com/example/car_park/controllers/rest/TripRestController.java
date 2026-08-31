package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.response.ErrorResponse;
import com.example.car_park.controllers.dto.response.GeoJsonFeatureCollection;
import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.controllers.dto.response.VehicleLocationJsonDto;
import com.example.car_park.controllers.providers.TripProvider;
import com.example.car_park.dao.model.User;
import com.example.car_park.enums.Format;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Поездки", description = "Поездки автомобилей: загрузка, выборка за интервал и стриминг")
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
public class TripRestController {
    private final TripProvider tp;

    @PostMapping("/map")
    @Operation(
            summary = "Получить поездки в формате GeoJSON",
            description = "Принимает список идентификаторов поездок и возвращает по одной коллекции GeoJSON "
                    + "на каждую поездку."
    )
    @ApiResponse(responseCode = "200", description = "Поездки получены",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = GeoJsonFeatureCollection.class))))
    public ResponseEntity<?> findForMap(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Идентификаторы поездок",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(type = "integer", format = "int64", example = "1"))))
            @RequestBody List<Long> tIds) {
        return ResponseEntity.ok(tp.findTripsForMap(tIds));
    }

    @PostMapping("/save")
    @Operation(
            summary = "Загрузить поездку из файла",
            description = "Разбирает загруженный GPX-файл и сохраняет точки как поездку указанного автомобиля.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schemaProperties = @SchemaProperty(name = "file",
                                    schema = @Schema(type = "string", format = "binary",
                                            description = "Файл трека в формате GPX"))))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поездка сохранена",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Поездка сохранена"))),
            @ApiResponse(responseCode = "400", description = "Файл не удалось разобрать или сохранить",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Автомобиль не найден")))
    })
    public ResponseEntity<?> saveFromFile(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                          @Parameter(description = "Идентификатор автомобиля", example = "1")
                                          @RequestParam("id") Long vId,
                                          @Parameter(hidden = true) @RequestParam("file") MultipartFile f) {
        try{
            tp.saveFromFile(u, vId, f);
            return ResponseEntity.ok("Поездка сохранена");
        } catch (RuntimeException e) {
            log.error("Ошибка при загрузке поездки для авто id={}", vId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{vehicle_id}/trips_points")
    @Operation(
            summary = "Получить точки поездок за интервал",
            description = """
                    Возвращает точки всех поездок автомобиля, попавших в интервал.
                    Формат ответа зависит от параметра `format`:

                    * `json` (по умолчанию) — массив точек `VehicleLocationJsonDto`;
                    * `geoJson` — одна коллекция `GeoJsonFeatureCollection`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Точки найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(oneOf = {VehicleLocationJsonDto[].class, GeoJsonFeatureCollection.class}))),
            @ApiResponse(responseCode = "400", description = "Указано неизвестное значение format",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Автомобиль с указанным id не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> findByPoints(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                          @Parameter(description = "Идентификатор автомобиля", example = "1")
                                          @PathVariable("vehicle_id") Long vId,
                                          @Parameter(description = "Начало интервала", example = "2024-01-01T00:00:00Z")
                                          @RequestParam("begin") ZonedDateTime s,
                                          @Parameter(description = "Конец интервала", example = "2024-01-31T23:59:59Z")
                                          @RequestParam("end") ZonedDateTime b,
                                          @Parameter(description = "Формат ответа (json или geoJson)",
                                                  schema = @Schema(type = "string", defaultValue = "json",
                                                          allowableValues = {"json", "geoJson"}))
                                          @RequestParam(defaultValue = "json", required = false) String format) {
        return ResponseEntity.ok(tp.findByPointsInInterval(u, vId, s, b, Format.getByValue(format)));
    }

    @GetMapping("/{vehicle_id}/trips")
    @Operation(
            summary = "Получить поездки за интервал",
            description = "Возвращает поездки автомобиля, попавшие в интервал, с адресами начальной и конечной "
                    + "точек."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Поездки найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TripDto.class)))),
            @ApiResponse(responseCode = "404", description = "Автомобиль с указанным id не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TripDto>> find(@Parameter(hidden = true) @AuthenticationPrincipal User user,
                                              @Parameter(description = "Идентификатор автомобиля", example = "1")
                                              @PathVariable("vehicle_id") Long vId,
                                              @Parameter(description = "Начало интервала", example = "2024-01-01T00:00:00Z")
                                              @RequestParam("begin") ZonedDateTime s,
                                              @Parameter(description = "Конец интервала", example = "2024-01-31T23:59:59Z")
                                              @RequestParam("end") ZonedDateTime b) {
        return ResponseEntity.ok(tp.findInIntervalForRest(user, vId, s, b));
    }

    @GetMapping(value = "/{vehicle_id}/online", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Отслеживание автомобиля",
            description = "Бесконечный поток Server-Sent Events: каждое событие — очередная точка автомобиля."
    )
    @ApiResponse(responseCode = "200", description = "Поток событий открыт",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = VehicleLocationJsonDto.class))))
    public Flux<VehicleLocationJsonDto> streamVehicleLocation(
            @Parameter(description = "Идентификатор автомобиля", example = "1")
            @PathVariable("vehicle_id") Long vId) {
        return tp.streamLocation(vId);
    }
}
