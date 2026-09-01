package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.ErrorResponse;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.controllers.providers.VehicleProvider;
import com.example.car_park.dao.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/vehicles")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Tag(name = "Автомобили", description = "Управление автомобилями предприятий, доступных менеджеру")
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
public class VehicleRestController {
    private final VehicleProvider vp;

    @GetMapping({"", "/{id}"})
    @Operation(
            summary = "Получить список автомобилей",
            description = """
                    Без `id` (`GET /api/vehicles`) возвращает страницу автомобилей всех предприятий,
                    доступных текущему менеджеру. Параметры постраничного вывода по умолчанию:
                    `size=5`, `sort=price,asc`.

                    С `id` (`GET /api/vehicles/{id}`) возвращает один автомобиль.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Автомобиль найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Автомобиль с указанным id не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> find(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                  @Parameter(description = "Идентификатор автомобиля. Опускается при запросе списка", example = "1")
                                  @PathVariable(required = false) Long id,
                                  @ParameterObject
                                  @PageableDefault(size = 5, sort = "price", direction = Sort.Direction.ASC) Pageable p) {
        if (id == null) {
            return ResponseEntity.ok(vp.findAllForRest(u, p));
        }
        return ResponseEntity.ok(vp.findByIdForRest(u, id));
    }

    @PostMapping("/new")
    @Operation(
            summary = "Создать автомобиль",
            description = "Создаёт автомобиль. Тело ответа пустое, идентификатор созданной записи возвращается "
                    + "в заголовке `Location`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Автомобиль создан",
                    content = @Content,
                    headers = @Header(name = "Location", description = "Ссылка на созданный автомобиль",
                            schema = @Schema(type = "string", example = "/api/vehicles/1"))),
            @ApiResponse(responseCode = "400", description = "Тело запроса не прошло валидацию либо указан "
                    + "несуществующий бренд, предприятие или водитель",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> create(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                    @Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.created(URI.create("/api/vehicles/" + vp.create(u, dto))).build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Изменить автомобиль",
            description = "Перезаписывает данные автомобиля значениями из тела запроса."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Автомобиль обновлён",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Тело запроса не прошло валидацию либо указан "
                    + "несуществующий бренд, предприятие или водитель",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Автомобиль с указанным id не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponseDto> edit(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                                   @Parameter(description = "Идентификатор автомобиля", example = "1")
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.ok(vp.edit(u, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить автомобиль", description = "Удаляет автомобиль вместе со связанными поездками.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Автомобиль удалён", content = @Content),
            @ApiResponse(responseCode = "404", description = "Автомобиль с указанным id не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> delete(@Parameter(hidden = true) @AuthenticationPrincipal User user,
                                    @Parameter(description = "Идентификатор автомобиля", example = "1")
                                    @PathVariable Long id) {
        vp.delete(user, id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/{id}/track")
//    public ResponseEntity<?> trackVehicle(@AuthenticationPrincipal User user,
//                                          @PathVariable Long id,
//                                          @RequestParam ZonedDateTime begin,
//                                          @RequestParam ZonedDateTime end,
//                                          @RequestParam(defaultValue = "json", required = false) String format) {
//        List<VehicleLocationJsonDto> vehicleLocationDtoList = vehicleService.getTrack(user, id, begin, end, format);
//        if ("geoJson".equalsIgnoreCase(format)) {
//            List<Map<String, Object>> features = new ArrayList<>();
//            for (VehicleLocationJsonDto loc : vehicleLocationDtoList) {
//                Point p = loc.getGeometry();
//                Map<String, Object> geometry = Map.of(
//                        "type", "Point",
//                        "coordinates", List.of(p.getX(), p.getY()) // X=долгота, Y=широта
//                );
//                Map<String, Object> properties = new LinkedHashMap<>();
//                properties.put("name", loc.getTimestamp());
//                properties.put("description", "Coordinates");
//                Map<String, Object> feature = Map.of(
//                        "type", "Feature",
//                        "geometry", geometry,
//                        "properties", properties
//                );
//                features.add(feature);
//            }
//            return ResponseEntity.ok(Map.of(
//                    "type", "FeatureCollection",
//                    "features", features
//            ));
//        }
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(vehicleLocationDtoList);
//    }
}
