package com.aerosaga.backend.workflow;

import com.aerosaga.backend.TemporalWorkerConfig;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DroneMissionWorkflowTest {

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
    void missionShouldCompleteSuccessfully() {
        Worker worker = testEnvironment.newWorker(
                TemporalWorkerConfig.TASK_QUEUE
        );

        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
                new DroneActivitiesImpl()
        );

        testEnvironment.start();

        DroneMissionWorkflow workflow = workflowClient.newWorkflowStub(
                DroneMissionWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TemporalWorkerConfig.TASK_QUEUE)
                        .setWorkflowId("mission-test-001")
                        .build()
        );

        String result = workflow.runMission(
                new MissionRequest("drone-001", 17.3850, 78.4867)
        );

        assertEquals(
                "Mission completed successfully for drone: drone-001",
                result
        );

        assertEquals(
                "droneId=drone-001, step=RETURN, status=COMPLETED",
                workflow.getState()
        );
    }

    @Test
    void missionShouldFailWhenTakeoffFails() {
        Worker worker = testEnvironment.newWorker(
                TemporalWorkerConfig.TASK_QUEUE
        );

        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(new DroneActivities() {
            @Override
            public String takeoff(String droneId) {
                throw new IllegalStateException(
                        "Simulated takeoff failure"
                );
            }

            @Override
            public String navigate(
                    String droneId,
                    double destLat,
                    double destLon
            ) {
                return "Navigated";
            }

            @Override
            public String dropPackage(String droneId) {
                return "Package delivered";
            }

            @Override
            public String returnToBase(String droneId) {
                return "Returned to base";
            }
        });

        testEnvironment.start();

        DroneMissionWorkflow workflow = workflowClient.newWorkflowStub(
                DroneMissionWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TemporalWorkerConfig.TASK_QUEUE)
                        .setWorkflowId("mission-test-failure-001")
                        .build()
        );

        assertThrows(
                io.temporal.client.WorkflowFailedException.class,
                () -> workflow.runMission(
                        new MissionRequest(
                                "drone-002",
                                17.3850,
                                78.4867
                        )
                )
        );

        assertEquals(
                "droneId=drone-002, step=TAKEOFF, status=FAILED",
                workflow.getState()
        );
    }

}
