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
                                    .setInitialInterval(Duration.ofSeconds(2))
                                    .setBackoffCoefficient(2.0)
                                    .setMaximumInterval(Duration.ofSeconds(10))
                                    .setMaximumAttempts(3)
                                    .build()
                    )
                    .build()
    );

    private final DroneActivities navigationActivities =
            Workflow.newActivityStub(
                    DroneActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofMinutes(12))
                            .setHeartbeatTimeout(Duration.ofSeconds(15))

                                    .setRetryOptions(
                                            io.temporal.common.RetryOptions.newBuilder()
                                                    .setInitialInterval(Duration.ofSeconds(2))
                                                    .setBackoffCoefficient(2.0)
                                                    .setMaximumInterval(Duration.ofSeconds(10))
                                                    .setMaximumAttempts(3)
                                                    .build()
                                    )


                    .build()
            );

    private String droneId;
    private String currentStep = "TAKEOFF";
    private String status = "PENDING";

    private void reportState(String parentWorkflowId) {
        if (parentWorkflowId != null
                && !parentWorkflowId.isBlank()) {

            FleetMissionWorkflow fleetWorkflow =
                    Workflow.newExternalWorkflowStub(
                            FleetMissionWorkflow.class,
                            parentWorkflowId
                    );

            fleetWorkflow.updateDroneState(
                    droneId,
                    currentStep,
                    status
            );
        }
    }

    @Override
    public String runMission(MissionRequest request) {
        droneId = request.droneId;
        status = "ACTIVE";
        reportState(request.parentWorkflowId);

        try {
            currentStep = "TAKEOFF";
            reportState(request.parentWorkflowId);
            activities.takeoff(droneId);

            currentStep = "NAVIGATE";
            reportState(request.parentWorkflowId);
            navigationActivities.navigate(
                    droneId,
                    request.destLat,
                    request.destLon
            );

            currentStep = "DROP";
            reportState(request.parentWorkflowId);
            activities.dropPackage(droneId);

            currentStep = "RETURN";
            reportState(request.parentWorkflowId);
            activities.returnToBase(droneId);

            status = "COMPLETED";
            reportState(request.parentWorkflowId);

            return "Mission completed successfully for drone: "
                    + droneId;

        } catch (RuntimeException exception) {
            status = "FAILED";
            reportState(request.parentWorkflowId);
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
