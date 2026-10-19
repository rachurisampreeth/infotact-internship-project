
package com.aerosaga.backend;

import com.aerosaga.backend.config.TemporalConfig;
import com.aerosaga.backend.workflow.DroneActivitiesImpl;
import com.aerosaga.backend.workflow.DroneMissionWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "temporal.worker.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TemporalWorkerConfig {

    @Bean
    public WorkflowServiceStubs workflowServiceStubs(
            TemporalConfig temporalConfig) {

        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalConfig.getTarget())
                        .build()
        );
    }

    @Bean
    public WorkflowClient workflowClient(
            WorkflowServiceStubs serviceStubs,
            TemporalConfig temporalConfig) {

        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
                .setNamespace(temporalConfig.getNamespace())
                .build();

        return WorkflowClient.newInstance(serviceStubs, options);
    }

    @Bean(destroyMethod = "shutdown")
    public WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            TemporalConfig temporalConfig) {

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(temporalConfig.getTaskQueue());
        worker.registerWorkflowImplementationTypes(
                DroneMissionWorkflowImpl.class);
        worker.registerActivitiesImplementations(
                new DroneActivitiesImpl());

        factory.start();
        return factory;
    }
}
