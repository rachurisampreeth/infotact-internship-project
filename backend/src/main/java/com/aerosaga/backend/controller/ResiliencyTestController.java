package com.aerosaga.backend.controller;

import com.aerosaga.backend.config.TemporalConfig;
import com.aerosaga.backend.workflow.ResiliencyTestWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resiliency-test")
public class ResiliencyTestController {

    private final WorkflowClient workflowClient;
    private final TemporalConfig temporalConfig;

    public ResiliencyTestController(
            WorkflowClient workflowClient,
            TemporalConfig temporalConfig) {
        this.workflowClient = workflowClient;
        this.temporalConfig = temporalConfig;
    }

    @PostMapping("/{testId}")
    public ResponseEntity<Map<String, String>> startTest(
            @PathVariable String testId) {

        if (testId == null || !testId.matches("[a-zA-Z0-9-]+")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid testId"));
        }

        String workflowId = "resiliency-" + testId;

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(temporalConfig.getTaskQueue())
                .build();

        ResiliencyTestWorkflow workflow =
                workflowClient.newWorkflowStub(
                        ResiliencyTestWorkflow.class, options);

        WorkflowClient.start(workflow::runTest);

        return ResponseEntity.accepted().body(Map.of(
                "workflowId", workflowId,
                "status", "STARTED"));
    }

    @GetMapping("/{testId}/state")
    public ResponseEntity<Map<String, String>> getState(
            @PathVariable String testId) {

        String workflowId = "resiliency-" + testId;

        ResiliencyTestWorkflow workflow =
                workflowClient.newWorkflowStub(
                        ResiliencyTestWorkflow.class, workflowId);

        return ResponseEntity.ok(Map.of(
                "workflowId", workflowId,
                "state", workflow.getState()));
    }
}
