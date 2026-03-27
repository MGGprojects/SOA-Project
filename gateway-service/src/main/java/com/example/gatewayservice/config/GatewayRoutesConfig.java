package com.example.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouterFunction<ServerResponse> authRoutes() {
        return route("auth-service")
                .route(request -> request.path().startsWith("/api/auth/"), http())
                .before(uri("http://localhost:8081"))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> userRoutes() {
        return route("user-service")
                .route(request -> request.path().startsWith("/api/users/"), http())
                .before(uri("http://localhost:8086"))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> businessRoutes() {
        return route("business-service")
                .route(request -> request.path().startsWith("/api/businesses/"), http())
                .before(uri("http://localhost:8083"))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> eventRoutes() {
        return route("event-service")
                .route(request -> request.path().startsWith("/api/events/") || "/api/events".equals(request.path()), http())
                .before(uri("http://localhost:8082"))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> calendarExportRoutes() {
        return route("calendar-export-service")
                .route(request -> request.path().startsWith("/api/exports/"), http())
                .before(uri("http://localhost:8085"))
                .build();
    }
}
