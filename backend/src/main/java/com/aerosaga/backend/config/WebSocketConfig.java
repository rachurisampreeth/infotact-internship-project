package com.aerosaga.backend.config;

import com.aerosaga.backend.websocket.TelemetryWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private final TelemetryWebSocketHandler telemetryWebSocketHandler;

    public WebSocketConfig(
            TelemetryWebSocketHandler telemetryWebSocketHandler) {
        this.telemetryWebSocketHandler = telemetryWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry) {

        registry.addHandler(
                telemetryWebSocketHandler,
                "/ws/telemetry"
        ).setAllowedOrigins("http://localhost:5173");
    }
}