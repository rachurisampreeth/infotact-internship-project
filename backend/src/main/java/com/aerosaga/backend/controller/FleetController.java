package com.aerosaga.backend.controller;

import com.aerosaga.backend.model.MissionRequest;
import com.aerosaga.backend.workflow.DroneMissionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fleets")
@CrossOrigin(origins = "http://localhost:5173")
public class FleetController {

    private static final String TASK_QUEUE = "DRONE_MISSION_QUEUE";

    private final WorkflowClient workflowClient;

    public FleetController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping
    public ResponseEntity<?> startFleetMissions(
            @RequestBody List<MissionRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "At least one drone mission is required"));
        }

        for (MissionRequest request : requests) {
            if (request.getDroneId() == null
                    || request.getDroneId().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Every mission must have a droneId"));
            }
        }

        List<Map<String, String>> startedMissions = new ArrayList<>();

        for (MissionRequest request : requests) {
            String workflowId = "mission-" + request.getDroneId();

            try {
                WorkflowOptions options = WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(TASK_QUEUE)
                        .build();

                DroneMissionWorkflow workflow =
                        workflowClient.newWorkflowStub(
                                DroneMissionWorkflow.class, options);

                com.aerosaga.backend.workflow.MissionRequest workflowRequest =
                        new com.aerosaga.backend.workflow.MissionRequest(
                                request.getDroneId(),
                                request.getDestLat(),
                                request.getDestLon()
                        );

                WorkflowClient.start(workflow::runMission, workflowRequest);

                startedMissions.add(Map.of(
                        "workflowId", workflowId,
                        "droneId", request.getDroneId(),
                        "status", "STARTED"
                ));

            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of(
                        "error", "Could not start mission for drone: "
                                + request.getDroneId(),
                        "details", e.getMessage() == null
                                ? "Unknown error" : e.getMessage(),
                        "startedMissions", startedMissions
                ));
            }
        }

        return ResponseEntity.accepted().body(Map.of(
                "status", "FLEET_STARTED",
                "count", startedMissions.size(),
                "missions", startedMissions
        ));
    }
}