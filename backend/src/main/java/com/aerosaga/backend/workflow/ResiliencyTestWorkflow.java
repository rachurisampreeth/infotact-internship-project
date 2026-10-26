package com.aerosaga.backend.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.QueryMethod;

@WorkflowInterface
public interface ResiliencyTestWorkflow {

    @WorkflowMethod
    String runTest();

    @QueryMethod
    String getState();
}
