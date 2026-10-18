package com.aerosaga.backend.controller;

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

    private static final String TASK_QUEUE = "DRONE_MISSION_QUEUE";

    private final WorkflowClient workflowClient;

    public MissionController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> startMission(
            @RequestBody MissionRequest request) {

        if (request.getDroneId() == null || request.getDroneId().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "droneId is required")
            );
        }

        String workflowId = "mission-" + request.getDroneId();

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

        return ResponseEntity.accepted().body(Map.of(
                "workflowId", workflowId,
                "status", "STARTED"
        ));
    }
}