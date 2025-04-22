package com.carenest.business.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("[Gateway 요청] {} {}", request.getMethod(), request.getURI());
        log.info("[요청 헤더] {}", request.getHeaders());

        return chain.filter(exchange)
                .doAfterTerminate(() -> {
                    // 예외 포함 응답 처리
                    ServerHttpResponse response = exchange.getResponse();
                    log.info("[Gateway 응답] 상태 코드: {}", response.getStatusCode());
                })
                .onErrorResume(error -> {
                    // 예외 발생 시 로깅
                    log.error("[Gateway 오류] {}", error.getMessage(), error);
                    return Mono.error(error);
                });
    }

    @Override
    public int getOrder() {
        return -1; // 가장 먼저 실행
    }
}