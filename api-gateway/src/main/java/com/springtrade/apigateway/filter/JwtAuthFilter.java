package com.springtrade.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secretKey;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("Executing JWT Authentication Filter");
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip authentication for public endpoints
            if(isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null) {
                log.warn("Missing Authorization header: {}", path);
                return unauthorizedResponse(exchange, "Missing Authorization header");
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header: {}", path);
                return unauthorizedResponse(exchange, "Invalid Authorization header");
            }

            String token = authHeader.substring(7); // Extract the token after "Bearer "

            try{
                Claims claims = extractClaimsFromToken(token);
                String userId = claims.get("userId", String.class);
                String email = claims.getSubject();
                if (userId == null || userId.isBlank() || email == null || email.isBlank()) {
                    log.warn("JWT missing required claims: {}", path);
                    return unauthorizedResponse(exchange, "Invalid JWT claims");
                }

                log.debug("Authenticated user: {} with email: {}", userId, email);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .build();

                return chain.filter(
                        exchange.mutate()
                                .request(modifiedRequest)
                                .build()
                );

            } catch (Exception e){
                log.warn("Invalid JWT token: {}", e.getMessage());
                return unauthorizedResponse(exchange, "Invalid JWT token");
            }

        };
    }

    private Claims extractClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/register") || path.startsWith("/register/") ||
                path.equals("/login") || path.startsWith("/login/") ||
                path.startsWith("/health") ||
                path.equals("/ws") || path.startsWith("/ws/");
    }

    private Key getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (RuntimeException ex) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized access: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Add any configuration properties if needed
    }
}
