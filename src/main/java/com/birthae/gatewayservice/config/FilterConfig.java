package com.birthae.gatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class FilterConfig {

//    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(r -> r.path("/api/v1/user/**") //r 이라는 값이 전달되면 패스확인하고
                            .filters(f -> f.addRequestHeader("user-request","user-request-header-value")
                                            .addResponseHeader("user-response","user-response-header-value"))    // 필터 적용해서
                            .uri("http://localhost:8081") ) // url로 이동시킨다
                .build();
    }
}
