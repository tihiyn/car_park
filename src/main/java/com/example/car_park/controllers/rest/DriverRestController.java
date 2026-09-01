package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.controllers.dto.response.ErrorResponse;
import com.example.car_park.controllers.providers.DriverProvider;
import com.example.car_park.dao.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Tag(name = "Водители", description = "Просмотр водителей предприятий, доступных менеджеру")
@SecurityRequirement(name = "jwtCookie")
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "Запрос без JWT либо водитель принадлежит "
                + "предприятию, недоступному менеджеру",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "302", description = "Пользователь аутентифицирован, но не имеет роли MANAGER: "
                + "отдаётся redirect на /error/forbidden, а не JSON",
                content = @Content)
})
public class DriverRestController {
    private final DriverProvider dp;

    @GetMapping({"", "/{id}"})
    @Operation(
            summary = "Получить список водителей",
            description = """
                    Без `id` (`GET /api/drivers`) возвращает страницу водителей всех предприятий,
                    доступных текущему менеджеру. Параметры постраничного вывода по умолчанию:
                    `size=5`, `sort=lastName,asc`.

                    С `id` (`GET /api/drivers/{id}`) возвращает одного водителя.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Водитель найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DriverDto.class))),
            @ApiResponse(responseCode = "404", description = "Водитель с указанным id не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> find(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                  @Parameter(description = "Идентификатор водителя. Опускается при запросе списка", example = "5")
                                  @PathVariable(required = false) Long id,
                                  @ParameterObject
                                  @PageableDefault(size = 5, sort = "lastName", direction = Sort.Direction.ASC) Pageable p) {
        if (id == null) {
            return ResponseEntity.ok(dp.findAllForRest(u, p));
        }
        return ResponseEntity.ok(dp.findByIdForRest(u, id));
    }
}
