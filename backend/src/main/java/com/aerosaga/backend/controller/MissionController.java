
package com.aerosaga.backend.controller;

import com.aerosaga.backend.config.TemporalConfig;
import com.aerosaga.backend.model.MissionRequest;
import com.aerosaga.backend.workflow.DroneMissionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:5173")
public class MissionController {

    private final WorkflowClient workflowClient;
    private final TemporalConfig temporalConfig;

    public MissionController(
            WorkflowClient workflowClient,
            TemporalConfig temporalConfig) {
        this.workflowClient = workflowClient;
        this.temporalConfig = temporalConfig;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> startMission(
            @RequestBody MissionRequest request) {

        if (request == null || request.getDroneId() == null
                || request.getDroneId().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "droneId is required"));
        }

        if (!Double.isFinite(request.getDestLat())
                || request.getDestLat() < -90
                || request.getDestLat() > 90) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "destLat must be between -90 and 90"));
        }

        if (!Double.isFinite(request.getDestLon())
                || request.getDestLon() < -180
                || request.getDestLon() > 180) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "destLon must be between -180 and 180"));
        }

        String workflowId = "mission-" + request.getDroneId();

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(temporalConfig.getTaskQueue())
                .build();

        DroneMissionWorkflow workflow =
                workflowClient.newWorkflowStub(
                        DroneMissionWorkflow.class, options);

        com.aerosaga.backend.workflow.MissionRequest workflowRequest =
                new com.aerosaga.backend.workflow.MissionRequest(
                        request.getDroneId(),
                        request.getDestLat(),
                        request.getDestLon());

        WorkflowClient.start(workflow::runMission, workflowRequest);

        return ResponseEntity.accepted().body(Map.of(
                "workflowId", workflowId,
                "status", "STARTED"));
    }
}
