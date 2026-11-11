package com.aerosaga.backend.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DroneMissionWorkflow {

    @WorkflowMethod
    String runMission(MissionRequest request);

    @QueryMethod(name = "getState")
    String getState();
}

