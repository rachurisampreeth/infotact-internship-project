package com.aerosaga.backend.workflow;

import com.aerosaga.backend.TemporalWorkerConfig;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetMissionWorkflowTest {

    private TestWorkflowEnvironment testEnvironment;
    private WorkflowClient workflowClient;

    @BeforeEach
    void setUp() {
        testEnvironment = TestWorkflowEnvironment.newInstance();
        workflowClient = testEnvironment.getWorkflowClient();
    }

    @AfterEach
    void tearDown() {
        testEnvironment.close();
    }

    @Test
    void fleetShouldCompleteMultipleDroneMissions() {
        Worker worker = testEnvironment.newWorker(
                TemporalWorkerConfig.TASK_QUEUE
        );

        worker.registerWorkflowImplementationTypes(
                FleetMissionWorkflowImpl.class,
                DroneMissionWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
                new DroneActivitiesImpl(
                        Duration.ofMillis(100),
                        Duration.ofMillis(25)
                )
        );

        testEnvironment.start();

        FleetMissionWorkflow workflow =
                workflowClient.newWorkflowStub(
                        FleetMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(
                                        TemporalWorkerConfig.TASK_QUEUE
                                )
                                .setWorkflowId("fleet-test-001")
                                .build()
                );

        List<String> results = workflow.runFleet(
                List.of(
                        new MissionRequest(
                                "drone-001", 17.3850, 78.4867
                        ),
                        new MissionRequest(
                                "drone-002", 17.4399, 78.4983
                        ),
                        new MissionRequest(
                                "drone-003", 17.4401, 78.3489
                        )
                )
        );

        assertEquals(3, results.size());

        assertTrue(results.contains(
                "Mission completed successfully for drone: drone-001"
        ));

        assertTrue(results.contains(
                "Mission completed successfully for drone: drone-002"
        ));

        assertTrue(results.contains(
                "Mission completed successfully for drone: drone-003"
        ));
    }

    @Test
    void fleetStateQueryShouldTrackEveryDrone()
            throws InterruptedException {

        Worker worker = testEnvironment.newWorker(
                TemporalWorkerConfig.TASK_QUEUE
        );

        worker.registerWorkflowImplementationTypes(
                FleetMissionWorkflowImpl.class,
                DroneMissionWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
                new DroneActivitiesImpl(
                        Duration.ofSeconds(2),
                        Duration.ofMillis(250)
                )
        );

        testEnvironment.start();

        FleetMissionWorkflow workflow =
                workflowClient.newWorkflowStub(
                        FleetMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(
                                        TemporalWorkerConfig.TASK_QUEUE
                                )
                                .setWorkflowId("fleet-state-test-001")
                                .build()
                );

        List<MissionRequest> requests = List.of(
                new MissionRequest(
                        "drone-101", 17.3850, 78.4867
                ),
                new MissionRequest(
                        "drone-102", 17.4399, 78.4983
                ),
                new MissionRequest(
                        "drone-103", 17.4401, 78.3489
                )
        );

        WorkflowClient.start(workflow::runFleet, requests);

        Set<String> completedDrones = new HashSet<>();
        long deadline = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(20);

        while (System.nanoTime() < deadline
                && completedDrones.size() < 3) {

            List<DroneMissionState> states =
                    workflow.getFleetState();

            for (DroneMissionState state : states) {
                if ("COMPLETED".equals(state.status)) {
                    assertEquals("RETURN", state.currentStep);
                    completedDrones.add(state.droneId);
                }
            }

            if (completedDrones.size() < 3) {
                Thread.sleep(100);
            }
        }

        assertEquals(
                Set.of("drone-101", "drone-102", "drone-103"),
                completedDrones,
                "The fleet query should report all three completed drones"
        );
    }
}
