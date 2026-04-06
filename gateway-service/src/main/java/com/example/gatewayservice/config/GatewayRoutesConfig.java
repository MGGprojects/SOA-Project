package com.example.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRoutesConfig {

    @Value("${auth-service.url}")
    private String authServiceUrl;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${business-service.url}")
    private String businessServiceUrl;

    @Value("${event-service.url}")
    private String eventServiceUrl;

    @Value("${calendar-export-service.url}")
    private String calendarExportServiceUrl;

    @Value("${notification-service.url}")
    private String notificationServiceUrl;

    @Bean
    RouterFunction<ServerResponse> authRoutes() {
        return route("auth-service")
                .route(request -> request.path().startsWith("/api/auth/"), http())
                .before(uri(authServiceUrl))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> userRoutes() {
        return route("user-service")
                .route(request -> request.path().startsWith("/api/users/") || "/api/users".equals(request.path()), http())
                .before(uri(userServiceUrl))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> businessRoutes() {
        return route("business-service")
                .route(request -> request.path().startsWith("/api/businesses/") || "/api/businesses".equals(request.path()), http())
                .before(uri(businessServiceUrl))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> eventRoutes() {
        return route("event-service")
                .route(request -> request.path().startsWith("/api/events/") || "/api/events".equals(request.path()), http())
                .before(uri(eventServiceUrl))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> calendarExportRoutes() {
        return route("calendar-export-service")
                .route(request -> request.path().startsWith("/api/exports/"), http())
                .before(uri(calendarExportServiceUrl))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> notificationRoutes() {
        return route("notification-service")
                .route(request -> request.path().startsWith("/api/notifications/") || "/api/notifications".equals(request.path()), http())
                .before(uri(notificationServiceUrl))
                .build();
    }
}
