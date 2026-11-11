package com.aerosaga.backend.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DroneActivities {

    @ActivityMethod
    String takeoff(String droneId);

    @ActivityMethod
    void navigate(String droneId, double destLat, double destLon);

    @ActivityMethod
    void dropPackage(String droneId);

    @ActivityMethod
    void returnToBase(String droneId);
}
