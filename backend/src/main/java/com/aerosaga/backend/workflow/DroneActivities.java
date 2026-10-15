
        package com.aerosaga.backend.workflow;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DroneActivities {

    @ActivityMethod
    String takeoff(String droneId);

    @ActivityMethod
    String navigate(String droneId, double destLat, double destLon);

    @ActivityMethod
    String dropPackage(String droneId);

    @ActivityMethod
    String returnToBase(String droneId);
}

