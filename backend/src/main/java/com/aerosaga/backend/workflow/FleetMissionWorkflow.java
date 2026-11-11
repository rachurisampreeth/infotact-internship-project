package com.aerosaga.backend.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface FleetMissionWorkflow {

    @WorkflowMethod
    List<String> runFleet(List<MissionRequest> requests);

    @QueryMethod(name = "getFleetState")
    List<DroneMissionState> getFleetState();

    @SignalMethod
    void updateDroneState(
            String droneId,
            String currentStep,
            String status
    );
}

