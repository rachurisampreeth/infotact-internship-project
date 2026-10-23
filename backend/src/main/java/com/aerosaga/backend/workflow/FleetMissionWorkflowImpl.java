package com.aerosaga.backend.workflow;

import com.aerosaga.backend.TemporalWorkerConfig;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FleetMissionWorkflowImpl implements FleetMissionWorkflow {

    private final Map<String, DroneMissionState> droneStates =
            new LinkedHashMap<>();

    @Override
    public List<String> runFleet(List<MissionRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one drone mission is required"
            );
        }

        Set<String> droneIds = new HashSet<>();

        for (MissionRequest request : requests) {
            if (request == null
                    || request.droneId == null
                    || request.droneId.isBlank()) {
                throw new IllegalArgumentException(
                        "Every mission must have a valid drone ID"
                );
            }

            if (!droneIds.add(request.droneId)) {
                throw new IllegalArgumentException(
                        "Duplicate drone ID: " + request.droneId
                );
            }

            droneStates.put(
                    request.droneId,
                    new DroneMissionState(
                            request.droneId,
                            "TAKEOFF",
                            "PENDING"
                    )
            );
        }

        String fleetWorkflowId =
                Workflow.getInfo().getWorkflowId();

        List<Promise<String>> missionPromises = new ArrayList<>();

        for (MissionRequest request : requests) {

            DroneMissionWorkflow droneWorkflow =
                    Workflow.newChildWorkflowStub(
                            DroneMissionWorkflow.class,
                            ChildWorkflowOptions.newBuilder()
                                    .setWorkflowId(
                                            fleetWorkflowId
                                                    + "-drone-"
                                                    + request.droneId
                                    )
                                    .setTaskQueue(
                                            TemporalWorkerConfig.TASK_QUEUE
                                    )
                                    .build()
                    );

            MissionRequest childRequest = new MissionRequest(
                    request.droneId,
                    request.destLat,
                    request.destLon,
                    fleetWorkflowId
            );

            Promise<String> missionPromise = Async.function(
                    () -> droneWorkflow.runMission(childRequest)
            );

            missionPromises.add(missionPromise);
        }

        Promise.allOf(
                missionPromises.toArray(new Promise<?>[0])
        ).get();

        List<String> results = new ArrayList<>();

        for (Promise<String> promise : missionPromises) {
            results.add(promise.get());
        }

        return results;
    }

    @Override
    public void updateDroneState(
            String droneId,
            String currentStep,
            String status
    ) {
        if (!droneStates.containsKey(droneId)) {
            return;
        }

        droneStates.put(
                droneId,
                new DroneMissionState(
                        droneId,
                        currentStep,
                        status
                )
        );
    }

    @Override
    public List<DroneMissionState> getFleetState() {
        List<DroneMissionState> states = new ArrayList<>();

        for (DroneMissionState state : droneStates.values()) {
            states.add(
                    new DroneMissionState(
                            state.droneId,
                            state.currentStep,
                            state.status
                    )
            );
        }

        return states;
    }
}
