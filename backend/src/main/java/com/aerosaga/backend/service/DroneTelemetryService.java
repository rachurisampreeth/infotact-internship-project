package com.aerosaga.backend.service;

import com.aerosaga.backend.model.DroneTelemetry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DroneTelemetryService {

    private final ConcurrentHashMap<String, DroneTelemetry> latestTelemetry = new ConcurrentHashMap<>();

    public void updateTelemetry(DroneTelemetry telemetry) {
        if (telemetry != null && telemetry.getDroneId() != null) {
            latestTelemetry.put(telemetry.getDroneId(), telemetry);
        }
    }

    public DroneTelemetry getTelemetry(String droneId) {
        return latestTelemetry.get(droneId);
    }

    public Collection<DroneTelemetry> getAllTelemetry() {
        return latestTelemetry.values();
    }
}
