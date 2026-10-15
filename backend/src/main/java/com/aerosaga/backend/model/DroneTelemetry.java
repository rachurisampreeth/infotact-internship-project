package com.aerosaga.backend.model;

public class DroneTelemetry {

    private String droneId;
    private double lat;
    private double lon;
    private double alt;
    private int battery;
    private String step;
    private String status;
    private long timestamp;

    public DroneTelemetry() {
    }

    public DroneTelemetry(String droneId, double lat, double lon, double alt, int battery, String step, String status, long timestamp) {
        this.droneId = droneId;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.battery = battery;
        this.step = step;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
