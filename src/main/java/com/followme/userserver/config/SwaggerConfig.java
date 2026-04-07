package com.followme.userserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {
    

        // 1. 프론트엔드/외부용 API 그룹 (JWT 인증 필요)
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("1. Public API (프론트엔드용)")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    // 2. 백엔드 타 서비스용 API 그룹 (Internal Secret 인증 필요)
    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
                .group("2. Internal API (백엔드 통신용)")
                .pathsToMatch("/internal/v1/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8000").description("API Gateway (외부 진입점)"))
                .addServersItem(new Server().url("http://localhost:8080").description("User Server (내부 직접 접근)"))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .security(Collections.singletonList(securityRequirement))
                .info(new Info()
                        .title("User Service API")
                        .version("v1.0.0")
                        .description("사용자 및 배송 담당자 관리 API 명세서"));
    }
}