package com.aerosaga.backend.websocket;

import com.aerosaga.backend.model.DroneTelemetry;
import com.aerosaga.backend.service.DroneTelemetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private final DroneTelemetryService telemetryService;
    private final ObjectMapper objectMapper;
    private final WorkflowClient workflowClient;

    private final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();

    public TelemetryWebSocketHandler(
            DroneTelemetryService telemetryService,
            ObjectMapper objectMapper,
            WorkflowClient workflowClient) {
        this.telemetryService = telemetryService;
        this.objectMapper = objectMapper;
        this.workflowClient = workflowClient;
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
            var drones = new ArrayList<Map<String, Object>>();

            for (DroneTelemetry telemetry :
                    telemetryService.getAllTelemetry()) {

                Map<String, Object> drone = new LinkedHashMap<>();

                drone.put("droneId", telemetry.getDroneId());
                drone.put("lat", telemetry.getLat());
                drone.put("lon", telemetry.getLon());
                drone.put("alt", telemetry.getAlt());
                drone.put("battery", telemetry.getBattery());
                drone.put("step", telemetry.getStep());
                drone.put("status", telemetry.getStatus());
                drone.put("timestamp", telemetry.getTimestamp());

                addWorkflowState(drone, telemetry.getDroneId());

                drones.add(drone);
            }

            Map<String, Object> message = Map.of(
                    "type", "telemetry",
                    "drones", drones
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

    private void addWorkflowState(
            Map<String, Object> drone, String droneId) {

        if (droneId == null || droneId.isBlank()) {
            return;
        }

        try {
            String workflowId = "mission-" + droneId;

            WorkflowStub workflow =
                    workflowClient.newUntypedWorkflowStub(workflowId);

            String state = workflow.query("getState", String.class);

            if (state == null || state.isBlank()) {
                return;
            }

            for (String part : state.split(",\\s*")) {
                String[] keyValue = part.split("=", 2);

                if (keyValue.length != 2) {
                    continue;
                }

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if ("step".equals(key)) {
                    drone.put("step", value);
                } else if ("status".equals(key)) {
                    drone.put("status", value);
                }
            }
        } catch (Exception ignored) {
            // Keep telemetry available if no Temporal workflow exists.
        }
    }
}