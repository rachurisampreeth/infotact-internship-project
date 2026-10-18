
package com.aerosaga.backend;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.aerosaga.backend.workflow.DroneActivitiesImpl;
import com.aerosaga.backend.workflow.DroneMissionWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.aerosaga.backend.workflow.FleetMissionWorkflowImpl;
@Configuration
@ConditionalOnProperty(
        name = "temporal.worker.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TemporalWorkerConfig {

    public static final String TASK_QUEUE = "DRONE_MISSION_QUEUE";

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget("localhost:7233")
                        .build()
        );
    }

    @Bean
    public WorkflowClient workflowClient(
            WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs);
    }

    @Bean(destroyMethod = "shutdown")
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class,
                FleetMissionWorkflowImpl.class
        );
        worker.registerActivitiesImplementations(
                new DroneActivitiesImpl());

        factory.start();
        return factory;
    }
}