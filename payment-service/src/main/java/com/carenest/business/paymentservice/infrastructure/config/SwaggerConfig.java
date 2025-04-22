package com.carenest.business.paymentservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareNest Payment API")
                        .description("CareNest 프로젝트의 결제 기능을 위한 API 문서입니다.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CareNest Team")
                                .email("team@carenest.com")
                                .url("https://carenest.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8000").description("로컬 개발 서버"),
                        new Server().url("http://localhost:9040").description("결제 서비스 직접 접근")
                ));
    }
}