package com.birthae.gatewayservice.filter;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j(topic = "AuthFilter")
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    Environment env;

    public AuthFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config {

    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if( !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

            String token = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);  // "Bearer " 부분을 제외하고 나머지 부분만 가져옴
            }

//            String token = authorizationHeader.replace("Bearer", "");
            System.out.println("token : " + token);
            if(!isJwtValid(token)){
                return onError(exchange, "JWT 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        });

    }

    private boolean isJwtValid(String token) {
        boolean returnValue = true;

        String subject = null;

        try {
            JwtParser jwtParser = Jwts.parserBuilder()
                    .setSigningKey(env.getProperty("jwt.secret.key"))
                    .build();

            subject = jwtParser.parseClaimsJws(token).getBody().getSubject();
        } catch(Exception ex) {
            returnValue = false;
        }

        if( subject == null || subject.isEmpty()){
            returnValue = false;
        }


        return returnValue;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(err);

        return response.setComplete();
    }
}
