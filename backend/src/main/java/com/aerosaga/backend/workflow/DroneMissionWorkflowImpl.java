package com.aerosaga.backend.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow {

    private final DroneActivities activities = Workflow.newActivityStub(
            DroneActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(30))
                    .setRetryOptions(
                            io.temporal.common.RetryOptions.newBuilder()
                                    .setMaximumAttempts(1)
                                    .build()
                    )
                    .build()
    );

    private String droneId;
    private String currentStep = "TAKEOFF";
    private String status = "PENDING";

    @Override
    public String runMission(MissionRequest request) {
        droneId = request.droneId;
        status = "ACTIVE";

        try {
            currentStep = "TAKEOFF";
            activities.takeoff(droneId);

            currentStep = "NAVIGATE";
            activities.navigate(droneId, request.destLat, request.destLon);

            currentStep = "DROP";
            activities.dropPackage(droneId);

            currentStep = "RETURN";
            activities.returnToBase(droneId);

            status = "COMPLETED";

            return "Mission completed successfully for drone: " + droneId;

        } catch (RuntimeException exception) {
            status = "FAILED";
            throw exception;
        }
    }

    @Override
    public String getState() {
        return "droneId=" + droneId
                + ", step=" + currentStep
                + ", status=" + status;
    }


}
