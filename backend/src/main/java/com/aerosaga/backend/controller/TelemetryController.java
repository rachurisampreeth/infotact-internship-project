
package com.aerosaga.backend.controller;

import com.aerosaga.backend.model.DroneTelemetry;
import com.aerosaga.backend.service.DroneTelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private static final Set<String> VALID_STEPS =
            Set.of("TAKEOFF", "NAVIGATE", "DROP", "RETURN");

    private static final Set<String> VALID_STATUSES =
            Set.of("PENDING", "ACTIVE", "COMPLETED", "FAILED");

    private final DroneTelemetryService telemetryService;

    public TelemetryController(DroneTelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> receiveTelemetry(
            @RequestBody DroneTelemetry telemetry) {

        if (telemetry.getDroneId() == null
                || telemetry.getDroneId().isBlank()) {
            return badRequest("droneId is required");
        }

        if (!Double.isFinite(telemetry.getLat())
                || !Double.isFinite(telemetry.getLon())
                || telemetry.getLat() < -90
                || telemetry.getLat() > 90
                || telemetry.getLon() < -180
                || telemetry.getLon() > 180) {
            return badRequest("Invalid latitude or longitude");
        }

        if (!Double.isFinite(telemetry.getAlt())
                || telemetry.getAlt() < 0) {
            return badRequest("alt must be a finite non-negative value");
        }

        if (telemetry.getBattery() < 0
                || telemetry.getBattery() > 100) {
            return badRequest("battery must be between 0 and 100");
        }

        if (telemetry.getStep() == null
                || !VALID_STEPS.contains(telemetry.getStep())) {
            return badRequest("Invalid step");
        }

        if (telemetry.getStatus() == null
                || !VALID_STATUSES.contains(telemetry.getStatus())) {
            return badRequest("Invalid status");
        }

        if (telemetry.getTimestamp() <= 0) {
            return badRequest("timestamp must be epoch milliseconds");
        }

        telemetryService.updateTelemetry(telemetry);

        return ResponseEntity.ok(Map.of(
                "status", "received",
                "droneId", telemetry.getDroneId()
        ));
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(
                Map.of("error", message)
        );
    }
}
