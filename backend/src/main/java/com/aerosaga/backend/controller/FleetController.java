
package com.aerosaga.backend.controller;

import com.aerosaga.backend.TemporalWorkerConfig;
import com.aerosaga.backend.model.FleetRequest;
import com.aerosaga.backend.model.MissionRequest;
import com.aerosaga.backend.workflow.FleetMissionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/fleets")
@CrossOrigin(origins = "http://localhost:5173")
public class FleetController {

    private final WorkflowClient workflowClient;

    public FleetController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping
    public ResponseEntity<?> startFleet(@RequestBody FleetRequest request) {

        if (request == null
                || request.getFleetId() == null
                || request.getFleetId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "fleetId is required"));
        }

        List<MissionRequest> drones = request.getDrones();

        if (drones == null || drones.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "At least one drone is required"));
        }

        Set<String> droneIds = new HashSet<>();

        for (MissionRequest drone : drones) {
            if (drone == null || drone.getDroneId() == null
                    || drone.getDroneId().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Every drone must have a droneId"));
            }

            if (!droneIds.add(drone.getDroneId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Duplicate droneId: "
                                + drone.getDroneId()));
            }
        }

        String workflowId = "fleet-" + request.getFleetId();

        List<com.aerosaga.backend.workflow.MissionRequest> workflowRequests =
                new ArrayList<>();

        for (MissionRequest drone : drones) {
            workflowRequests.add(
                    new com.aerosaga.backend.workflow.MissionRequest(
                            drone.getDroneId(),
                            drone.getDestLat(),
                            drone.getDestLon(),
                            workflowId
                    )
            );
        }

        try {
            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setWorkflowId(workflowId)
                    .setTaskQueue(TemporalWorkerConfig.TASK_QUEUE)
                    .build();

            FleetMissionWorkflow workflow =
                    workflowClient.newWorkflowStub(
                            FleetMissionWorkflow.class, options);

            WorkflowClient.start(workflow::runFleet, workflowRequests);

            return ResponseEntity.accepted().body(Map.of(
                    "status", "FLEET_STARTED",
                    "fleetId", request.getFleetId(),
                    "workflowId", workflowId,
                    "count", workflowRequests.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Could not start fleet workflow",
                    "details", e.getMessage() == null
                            ? "Unknown error" : e.getMessage()
            ));
        }
    }
}
