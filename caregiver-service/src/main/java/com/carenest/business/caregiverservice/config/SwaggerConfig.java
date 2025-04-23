package com.carenest.business.caregiverservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {



	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.servers(List.of(
				new Server().url("http://localhost:9020")
			));
	}

}