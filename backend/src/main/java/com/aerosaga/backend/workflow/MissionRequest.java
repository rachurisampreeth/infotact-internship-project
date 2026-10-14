package com.aerosaga.backend.workflow;

public class MissionRequest {
    public String droneId;
    public double destLat;
    public double destLon;

    public MissionRequest() {}   // Temporal needs an empty constructor

    public MissionRequest(String droneId, double destLat, double destLon) {
        this.droneId = droneId;
        this.destLat = destLat;
        this.destLon = destLon;
    }
}
