package com.example.DATN_Fashion_Shop_BE.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import
        io. swagger. v3.oas. models. parameters. Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Fashion-Shop API",
                version = "1.0.0",
                description = "Fashion Shop API Documentation"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server"),
                @Server(url = "http://45.117.179.16:8088", description = "Production Server")
        }
)
@SecurityScheme(
        name = "bearer-key",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
        @Bean
        public OpenApiCustomizer globalHeaderCustomizer() {
                return openApi -> openApi.getPaths().values().forEach(pathItem ->
                        pathItem.readOperations().forEach(operation -> {
                                Parameter acceptLanguageHeader = new Parameter()
                                        .name("Accept-Language")
                                        .description("Language preference (e.g., en, vi)")
                                        .required(false)
                                        .in("header")
                                        .schema(new StringSchema());
                                operation.addParametersItem(acceptLanguageHeader);
                        }));
        }
}
