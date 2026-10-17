package com.aerosaga.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "temporal.worker.enabled=false")
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}