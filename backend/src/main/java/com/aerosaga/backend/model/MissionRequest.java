package com.aerosaga.backend.model;

public class MissionRequest {

    public String droneId;
    public double destLat;
    public double destLon;

    public MissionRequest() {
    }

    public MissionRequest(String droneId, double destLat, double destLon) {
        this.droneId = droneId;
        this.destLat = destLat;
        this.destLon = destLon;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public double getDestLat() {
        return destLat;
    }

    public void setDestLat(double destLat) {
        this.destLat = destLat;
    }

    public double getDestLon() {
        return destLon;
    }

    public void setDestLon(double destLon) {
        this.destLon = destLon;
    }
}
