package com.aerosaga.backend.controller;

import com.aerosaga.backend.model.DroneTelemetry;
import com.aerosaga.backend.service.DroneTelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final DroneTelemetryService telemetryService;

    public TelemetryController(DroneTelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> receiveTelemetry(
            @RequestBody DroneTelemetry telemetry) {

        if (telemetry.getDroneId() == null
                || telemetry.getDroneId().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "droneId is required")
            );
        }

        if (telemetry.getLat() < -90 || telemetry.getLat() > 90
                || telemetry.getLon() < -180 || telemetry.getLon() > 180) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid latitude or longitude")
            );
        }

        telemetryService.updateTelemetry(telemetry);

        return ResponseEntity.ok(Map.of(
                "status", "received",
                "droneId", telemetry.getDroneId()
        ));
    }
}
