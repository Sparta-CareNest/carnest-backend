package com.carenest.business.gatewayservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // JWT 없이 통과시킬 경로 예외 처리
        if (path.equals("/api/v1/users/signup") || path.equals("/api/v1/users/login") ||
            path.equals("/api/v1/caregivers/search") || path.equals("/api/v1/caregivers/rating/top")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 인증 헤더가 없거나 유효하지 않으면 그냥 통과
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
            logger.warn("JWT token is missing or invalid");
            return chain.filter(exchange);
        }

        try {
            // Bearer 제거하여 token 추출
            String token = jwtUtil.substringToken(authHeader);
            logger.info("Validated JWT Token: {}", token);
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getUserInfoFromToken(token);

                // AuthUserInfo와 일치하는 맵 생성
                Map<String, Object> authUserMap = new HashMap<>();
                authUserMap.put("userId", claims.getSubject()); // userId는 subject로 설정됨
                authUserMap.put("email", claims.get("email", String.class));
                authUserMap.put("role", claims.get("role", String.class)); // role은 auth 키로 저장됨

                try {
                    String userJson = new ObjectMapper().writeValueAsString(authUserMap);
                    String encoded = Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8));

                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("AuthUser", encoded)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                } catch (Exception e) {
                    logger.error("Failed to serialize user info", e);
                }
            } else {
                // 유효하지 않은 토큰
                logger.warn("Invalid JWT token");
            }
        } catch (Exception e) {
            logger.error("JWT processing failed", e);
        }

        // 오류 발생 시 또는 토큰이 유효하지 않은 경우에도 요청 통과
        return chain.filter(exchange);
    }
}
