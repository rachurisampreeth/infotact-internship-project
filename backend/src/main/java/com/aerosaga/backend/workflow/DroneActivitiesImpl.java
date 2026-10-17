
        package com.aerosaga.backend.workflow;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;

import java.time.Duration;

public class DroneActivitiesImpl implements DroneActivities {

    private final Duration navigationDuration;
    private final Duration heartbeatInterval;

    public DroneActivitiesImpl() {
        this(Duration.ofMinutes(10), Duration.ofSeconds(5));
    }

    public DroneActivitiesImpl(
            Duration navigationDuration,
            Duration heartbeatInterval
    ) {
        if (navigationDuration.isZero() || navigationDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "Navigation duration must be positive"
            );
        }

        if (heartbeatInterval.isZero() || heartbeatInterval.isNegative()) {
            throw new IllegalArgumentException(
                    "Heartbeat interval must be positive"
            );
        }

        this.navigationDuration = navigationDuration;
        this.heartbeatInterval = heartbeatInterval;
    }

    private void simulateDelay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Drone activity was interrupted",
                    exception
            );
        }
    }

    @Override
    public String takeoff(String droneId) {
        simulateDelay(1000);
        return droneId + " took off successfully";
    }

    @Override
    public String navigate(String droneId, double destLat, double destLon) {
        ActivityExecutionContext context = Activity.getExecutionContext();

        long totalMillis = navigationDuration.toMillis();
        long intervalMillis = Math.max(1, heartbeatInterval.toMillis());
        long elapsedMillis = 0;

        while (elapsedMillis < totalMillis) {
            long delay = Math.min(
                    intervalMillis,
                    totalMillis - elapsedMillis
            );

            simulateDelay(delay);
            elapsedMillis += delay;

            context.heartbeat(
                    "Navigation progress: "
                            + elapsedMillis
                            + " ms for drone "
                            + droneId
            );
        }

        return droneId + " navigated to destination: "
                + destLat + ", " + destLon;
    }

    @Override
    public String dropPackage(String droneId) {
        simulateDelay(1000);
        return droneId + " delivered the package successfully";
    }

    @Override
    public String returnToBase(String droneId) {
        simulateDelay(1000);
        return droneId + " returned to base successfully";
    }
}
