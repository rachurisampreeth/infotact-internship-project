
package com.aerosaga.backend.workflow;

public class DroneActivitiesImpl implements DroneActivities {

    private void simulateDelay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Drone activity was interrupted", exception);
        }
    }

    @Override
    public String takeoff(String droneId) {
        simulateDelay(1000);
        return droneId + " took off successfully";
    }

    @Override
    public String navigate(String droneId, double destLat, double destLon) {
        simulateDelay(2000);
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

