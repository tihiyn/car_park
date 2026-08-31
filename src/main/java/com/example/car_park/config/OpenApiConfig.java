package com.example.car_park.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Car Park API",
                version = "v1",
                description = """
                        REST API системы учёта автопарка: предприятия, автомобили, водители, поездки и отчёты.

                        ## Аутентификация

                        Все эндпоинты `/api/**` требуют роль `MANAGER`.

                        Токен выдаётся эндпоинтом `POST /auth/login`.

                        ## Формат ошибок

                        Ошибки возвращаются в едином виде: `timestamp`, `status`, `error`, `message`, `path`.
                        """,
                contact = @Contact(name = "car_park")
        ),
        servers = @Server(url = "/", description = "Текущий хост")
)
@SecurityScheme(
        name = "jwtCookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "JWT",
        description = "JWT, выставленный эндпоинтом POST /auth/login в cookie с именем JWT"
)
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer collectionGetResponseCustomizer() {
        Map<String, String> collectionSchemas = Map.of(
                "/api/vehicles", "VehicleResponseDto",
                "/api/enterprises", "EnterpriseResponseDto",
                "/api/drivers", "DriverDto"
        );

        return openApi -> collectionSchemas.forEach((path, schemaName) -> {
            PathItem pathItem = openApi.getPaths() == null ? null : openApi.getPaths().get(path);
            if (pathItem == null || pathItem.getGet() == null || pathItem.getGet().getResponses() == null) {
                return;
            }
            ApiResponse ok = pathItem.getGet().getResponses().get("200");
            if (ok == null || ok.getContent() == null) {
                return;
            }
            ok.setDescription("Страница найденных записей");
            ok.getContent().values().forEach(media -> media.setSchema(
                    new ArraySchema().items(new Schema<>().$ref("#/components/schemas/" + schemaName))
            ));
        });
    }
    @Bean
    public OpenApiCustomizer oneOfTypeCleanupCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().stream()
                    .flatMap(pathItem -> pathItem.readOperations().stream())
                    .filter(operation -> operation.getResponses() != null)
                    .flatMap(operation -> operation.getResponses().values().stream())
                    .filter(response -> response.getContent() != null)
                    .flatMap(response -> response.getContent().values().stream())
                    .filter(media -> media.getSchema() != null && media.getSchema().getOneOf() != null)
                    .forEach(media -> {
                        Schema<?> original = media.getSchema();
                        media.setSchema(new Schema<>()
                                .oneOf(original.getOneOf())
                                .description(original.getDescription()));
                    });
        };
    }
}
