package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.request.EnterpriseRequestDto;
import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.controllers.dto.response.ErrorResponse;
import com.example.car_park.controllers.providers.EnterpriseProvider;
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
@RequestMapping("/api/enterprises")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Tag(name = "Предприятия", description = "Управление предприятиями, принадлежащими менеджеру")
@SecurityRequirement(name = "jwtCookie")
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "Запрос без JWT либо предприятие не закреплено "
                + "за менеджером",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "302", description = "Пользователь аутентифицирован, но не имеет роли MANAGER: "
                + "отдаётся redirect на /error/forbidden, а не JSON",
                content = @Content)
})
public class EnterpriseRestController {
    private final EnterpriseProvider ep;

    @GetMapping({"", "/{id}"})
    @Operation(
            summary = "Получить список предприятий",
            description = """
                    Без `id` (`GET /api/enterprises`) возвращает страницу предприятий текущего менеджера.
                    Параметры постраничного вывода по умолчанию: `size=20`, `sort=name,asc`.

                    С `id` (`GET /api/enterprises/{id}`) возвращает одно предприятие.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Предприятие найдено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EnterpriseResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Предприятие с указанным id не найдено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> find(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                  @Parameter(description = "Идентификатор предприятия. Опускается при запросе списка", example = "1")
                                  @PathVariable(required = false) Long id,
                                  @ParameterObject
                                  @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable p) {
        if (id == null) {
            return ResponseEntity.ok(ep.findAllForRest(u, p));
        }
        return ResponseEntity.ok(ep.findByIdForRest(u, id));
    }

    @PostMapping("/new")
    @Operation(
            summary = "Создать предприятие",
            description = "Создаёт предприятие и закрепляет его за менеджером. Тело ответа пустое, "
                    + "идентификатор созданной записи возвращается в заголовке `Location`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Предприятие создано",
                    content = @Content,
                    headers = @Header(name = "Location", description = "Ссылка на созданное предприятие",
                            schema = @Schema(type = "string", example = "/api/enterprises/1"))),
            @ApiResponse(responseCode = "400", description = "Тело запроса не прошло валидацию",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Предприятие с таким регистрационным номером уже существует",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> create(@Parameter(hidden = true) @AuthenticationPrincipal User user,
                                    @Valid @RequestBody EnterpriseRequestDto dto) {
        Long id = ep.create(user, dto);
        return ResponseEntity.created(URI.create("/api/enterprises/" + id)).build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Изменить предприятие",
            description = "Перезаписывает данные предприятия значениями из тела запроса."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Предприятие обновлено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EnterpriseResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Тело запроса не прошло валидацию",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Предприятие с указанным id не найдено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnterpriseResponseDto> edit(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                                      @Parameter(description = "Идентификатор предприятия", example = "1")
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody EnterpriseRequestDto dto) {
        return ResponseEntity.ok(ep.edit(u, id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить предприятие",
            description = "Удаляет предприятие вместе со связанными автомобилями и водителями.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Предприятие удалено", content = @Content),
            @ApiResponse(responseCode = "404", description = "Предприятие с указанным id не найдено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> delete(@Parameter(hidden = true) @AuthenticationPrincipal User u,
                                    @Parameter(description = "Идентификатор предприятия", example = "1")
                                    @PathVariable Long id) {
        ep.delete(u, id);
        return ResponseEntity.noContent().build();
    }
}
