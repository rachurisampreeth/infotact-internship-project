package com.aerosaga.backend.workflow;

import io.temporal.workflow.Workflow;
import java.time.Duration;

public class ResiliencyTestWorkflowImpl implements ResiliencyTestWorkflow {

    private String status = "RUNNING";

    @Override
    public String runTest() {
        Workflow.sleep(Duration.ofMinutes(10));
        status = "COMPLETED";
        return "Resiliency test completed successfully";
    }

    @Override
    public String getState() {
        return status;
    }
}
