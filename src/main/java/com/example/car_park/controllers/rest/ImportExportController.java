package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.response.ErrorResponse;
import com.example.car_park.controllers.dto.response.ExportResp;
import com.example.car_park.controllers.dto.response.ImportResp;
import com.example.car_park.controllers.providers.ImportExportProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/data")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Tag(name = "Импорт и экспорт", description = "Выгрузка данных предприятия в файл, загрузка данных предприятия из файла")
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
public class ImportExportController {
    private final ImportExportProvider p;

    @PostMapping("/export")
    @Operation(
            summary = "Выгрузить данные предприятия",
            description = "Выгружает предприятие вместе с автомобилями, водителями и поездками за интервал "
                    + "в файл. Имя файла передаётся в заголовке `Content-Disposition`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл сформирован",
                    headers = {
                            @Header(name = HttpHeaders.CONTENT_DISPOSITION,
                                    description = "Имя файла выгрузки",
                                    schema = @Schema(type = "string", example = "attachment; filename=\"enterprise-1.json\"")),
                            @Header(name = HttpHeaders.CONTENT_LENGTH, description = "Размер файла в байтах",
                                    schema = @Schema(type = "integer", format = "int64", example = "2048"))
                    },
                    content = {
                            @Content(mediaType = "text/json",
                                    schema = @Schema(type = "string", format = "binary")),
                            @Content(mediaType = "text/csv",
                                    schema = @Schema(type = "string", format = "binary"))
                    }),
            @ApiResponse(responseCode = "500", description = "Не удалось сформировать файл",
                    content = @Content)
    })
    public ResponseEntity<Resource> exportData(
            @Parameter(description = "Идентификатор выгружаемого предприятия", example = "1")
            @RequestParam Long enterpriseId,
            @Parameter(description = "Начало интервала, за который выгружаются поездки", example = "2024-01-01T00:00:00Z")
            @RequestParam ZonedDateTime begin,
            @Parameter(description = "Конец интервала, за который выгружаются поездки", example = "2024-01-31T23:59:59Z")
            @RequestParam ZonedDateTime end,
            @Parameter(description = "Формат выгрузки (csv или json)",
                    schema = @Schema(type = "string", defaultValue = "json", allowableValues = {"json", "csv"}))
            @RequestParam(defaultValue = "json", required = false) String format) {
        ExportResp resp = p.exportData(enterpriseId, begin, end, format);
        if (resp == null) {
            return ResponseEntity.internalServerError().body(null);
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(resp.getFileName()))
            .header(HttpHeaders.CONTENT_TYPE, "text/%s; charset=UTF-8".formatted(format.equalsIgnoreCase("csv") ? "csv" : "json"))
            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resp.getFileLength()))
            .body(resp.getRes());
    }

    @PostMapping("/import")
    @Operation(
            summary = "Загрузить данные из файла",
            description = "Загрузить данные из файла",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schemaProperties = @SchemaProperty(name = "file",
                                    schema = @Schema(type = "string", format = "binary",
                                            description = "Файл с ранее выгруженными данными"))))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Импорт завершён успешно",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Импорт завершён успешно"))),
            @ApiResponse(responseCode = "500", description = "Импорт не удался",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Не удалось разобрать файл")))
    })
    public ResponseEntity<String> importData(
            @Parameter(description = "Формат загружаемого файла",
                    schema = @Schema(type = "string", defaultValue = "json", allowableValues = {"json", "csv"}))
            @RequestParam(defaultValue = "json", required = false) String format,
            @Parameter(hidden = true) @RequestParam("file") MultipartFile file) {
        ImportResp resp = p.importData(format, file);
        if (resp.isSuccessful()) {
            return ResponseEntity.ok(resp.getDesc());
        }
        return ResponseEntity.internalServerError().body(resp.getDesc());
    }
}
