package com.aerosaga.backend.websocket;

import com.aerosaga.backend.model.DroneTelemetry;
import com.aerosaga.backend.service.DroneTelemetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private final DroneTelemetryService telemetryService;
    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();

    public TelemetryWebSocketHandler(
            DroneTelemetryService telemetryService,
            ObjectMapper objectMapper) {
        this.telemetryService = telemetryService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws IOException {
        sessions.add(session);
        sendTelemetry(session);
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastTelemetry() {
        for (WebSocketSession session : sessions) {
            sendTelemetry(session);
        }
    }

    private void sendTelemetry(WebSocketSession session) {
        if (!session.isOpen()) {
            sessions.remove(session);
            return;
        }

        try {
            Map<String, Object> message = Map.of(
                    "type", "telemetry",
                    "drones", new ArrayList<>(
                            telemetryService.getAllTelemetry())
            );

            String json = objectMapper.writeValueAsString(message);

            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            sessions.remove(session);
        }
    }
}