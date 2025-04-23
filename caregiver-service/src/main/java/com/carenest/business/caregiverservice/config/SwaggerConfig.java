package com.carenest.business.caregiverservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		// 서버 정보
		Server localServer = new Server()
			.url("http://localhost:9020")
			.description("로컬 개발 서버");

		// 문서 기본 정보
		Info apiInfo = new Info()
			.title("CareNest 간병인 서비스 API")
			.description("CareNest 플랫폼의 간병인 도메인 API 문서입니다.")
			.version("v1.0.0")
			.contact(new Contact()
				.name("CareNest Backend Team")
				.email("codejomo99@gmail.com")
				.url("https://github.com/Sparta-CareNest/carnest-backend"));

		return new OpenAPI()
			.info(apiInfo)
			.servers(List.of(localServer));
	}
}