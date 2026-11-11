package com.aerosaga.backend.controller;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:5173")
public class MissionStateController {

    private final WorkflowClient workflowClient;

    public MissionStateController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @GetMapping("/{droneId}/state")
    public ResponseEntity<?> getMissionState(
            @PathVariable String droneId,
            @RequestParam(required = false) String fleetId) {

        String workflowId;

        if (fleetId != null && !fleetId.isBlank()) {
            workflowId = "fleet-" + fleetId
                    + "-drone-" + droneId;
        } else {
            workflowId = "mission-" + droneId;
        }

        try {
            WorkflowStub workflow =
                    workflowClient.newUntypedWorkflowStub(workflowId);

            String state = workflow.query("getState", String.class);

            return ResponseEntity.ok(Map.of(
                    "workflowId", workflowId,
                    "droneId", droneId,
                    "state", state
            ));

        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Mission state unavailable",
                    "workflowId", workflowId,
                    "details", e.getMessage() == null
                            ? "Unknown error" : e.getMessage()
            ));
        }
    }
}