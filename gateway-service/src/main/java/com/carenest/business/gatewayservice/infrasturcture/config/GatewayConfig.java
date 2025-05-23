package com.carenest.business.gatewayservice.infrasturcture.config;

import com.carenest.business.gatewayservice.infrasturcture.security.JwtAuthFilter;
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
                .route("review-service", r -> r.path("/api/v1/reviews/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://review-service"))
                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://notification-service"))
                .route("caregiver-service", r -> r.path("/api/v1/caregivers/**","/api/v1/caregiver-approvals/**")
                    .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://caregiver-service"))
                .route("payment-service", r -> r.path("/api/v1/payments/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://payment-service"))
                .route("reservation-service", r -> r.path("/api/v1/reservations/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://reservation-service"))
                .route("chat-service", r -> r.path("/api/v1/chats/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://chat-service"))
                .route("admin-service", r -> r.path("/api/v1/admin/settlements/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://admin-service"))
                .build();
    }
}
