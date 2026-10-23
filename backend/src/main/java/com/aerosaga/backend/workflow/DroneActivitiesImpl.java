package com.aerosaga.backend.workflow;

public class DroneActivitiesImpl implements DroneActivities {


    @Override
    public String takeoff(String droneId) {
        return droneId + " took off successfully";
    }

    @Override
    public String navigate(String droneId, double destLat, double destLon) {
        return droneId + " navigated to destination: "
                + destLat + ", " + destLon;
    }

    @Override
    public String dropPackage(String droneId) {
        return droneId + " delivered the package successfully";
    }

    @Override
    public String returnToBase(String droneId) {
        return droneId + " returned to base successfully";
    }


}

