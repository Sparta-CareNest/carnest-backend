package com.carenest.business.gatewayservice;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public GatewayConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://user-service"))

                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://notification-service"))

                .route("caregiver-service", r -> r.path("/api/v1/caregivers/**","/internal/v1/caregivers/**")
                    .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://caregiver-service"))
                .build();
    }
}
