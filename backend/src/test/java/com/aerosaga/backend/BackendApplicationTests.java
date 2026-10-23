package com.aerosaga.backend;

import io.temporal.client.WorkflowClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "temporal.worker.enabled=false")
class BackendApplicationTests {

    @MockitoBean
    private WorkflowClient workflowClient;

    @Test
    void contextLoads() {
    }
}